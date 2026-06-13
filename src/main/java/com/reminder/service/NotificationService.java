package com.reminder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reminder.model.Notification;
import com.reminder.model.Reminder;
import com.reminder.model.User;
import com.reminder.repository.NotificationRepository;
import com.reminder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Notification.Builder;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;  // WebSocket
    private final PushService pushService;                   // Web Push
    private final SmsService smsService;
    private final ObjectMapper objectMapper;

    // Called by the scheduler when a reminder is due
    public void sendReminderNotifications(Reminder reminder) {
        User user = reminder.getUser();
        String message = buildMessage(reminder);

        // 1. In-app popup via WebSocket
        sendInAppNotification(reminder, user, message);

        // 2. Browser push notification (works when app is closed)
        if (user.getPushSubscription() != null) {
            sendPushNotification(reminder, user, message);
        }

        // 3. SMS if user has phone number
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
            smsService.sendSms(user.getPhoneNumber(), message);
        }
    }

    private void sendInAppNotification(Reminder reminder, User user, String message) {
        // Save to DB so user sees it in notification list
        Notification notification = Notification.builder()
                .reminder(reminder)
                .user(user)
                .type(Notification.NotificationType.IN_APP)
                .message(message)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        // Push to user's WebSocket channel — React listens on /user/{email}/notifications
        try {
            Map<String, Object> payload = Map.of(
                    "id", notification.getId(),
                    "title", reminder.getTitle(),
                    "message", message,
                    "reminderId", reminder.getId(),
                    "sentAt", notification.getSentAt().toString()
            );
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),
                    "/notifications",
                    payload
            );
        } catch (Exception e) {
            log.error("WebSocket send failed for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void sendPushNotification(Reminder reminder, User user, String message) {
        try {
            // Parse stored subscription JSON
            Subscription subscription = objectMapper.readValue(
                    user.getPushSubscription(), Subscription.class);

            String payload = objectMapper.writeValueAsString(Map.of(
                    "title", "Reminder: " + reminder.getTitle(),
                    "body", message,
                    "icon", "/logo192.png",
                    "url", "/dashboard"
            ));

            nl.martijndwars.webpush.Notification pushNotif =
                    new nl.martijndwars.webpush.Notification(subscription, payload);
            pushService.send(pushNotif);

            // Log it
            notificationRepository.save(Notification.builder()
                    .reminder(reminder).user(user)
                    .type(Notification.NotificationType.PUSH)
                    .message(message).sentAt(LocalDateTime.now())
                    .isRead(false).build());

        } catch (Exception e) {
            log.error("Push notification failed for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public List<Notification> getUnreadNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserIdAndIsReadFalseOrderBySentAtDesc(user.getId());
    }

    public long getUnreadCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    public void markAllRead(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Notification> unread = notificationRepository
                .findByUserIdAndIsReadFalseOrderBySentAtDesc(user.getId());
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    public void savePushSubscription(String userEmail, String subscriptionJson) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPushSubscription(subscriptionJson);
        userRepository.save(user);
    }

    private String buildMessage(Reminder reminder) {
        if (reminder.getLeadMinutes() > 0) {
            return reminder.getTitle() + " is in " + reminder.getLeadMinutes() + " minutes.";
        }
        return "Reminder: " + reminder.getTitle();
    }
}
