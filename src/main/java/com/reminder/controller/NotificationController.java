package com.reminder.controller;

import com.reminder.model.Notification;
import com.reminder.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications/unread
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnread(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user.getUsername()));
    }

    // GET /api/notifications/count
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails user) {
        long count = notificationService.getUnreadCount(user.getUsername());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // POST /api/notifications/mark-read
    @PostMapping("/mark-read")
    public ResponseEntity<Void> markAllRead(
            @AuthenticationPrincipal UserDetails user) {
        notificationService.markAllRead(user.getUsername());
        return ResponseEntity.ok().build();
    }

    // POST /api/notifications/push-subscription
    // React sends the browser's push subscription object here
    @PostMapping("/push-subscription")
    public ResponseEntity<Void> savePushSubscription(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody Map<String, Object> body) {
        try {
            String subscriptionJson = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(body);
            notificationService.savePushSubscription(user.getUsername(), subscriptionJson);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
