package com.reminder.service;

import com.reminder.model.Reminder;
import com.reminder.model.User;
import com.reminder.repository.ReminderRepository;
import com.reminder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public record ReminderRequest(
        String title,
        String description,
        LocalDateTime eventAt,
        Integer leadMinutes,
        Reminder.RecurrenceType recurrence
    ) {}

    public Reminder createReminder(String userEmail, ReminderRequest req) {
        User user = getUserByEmail(userEmail);

        // remindAt = eventAt minus lead time
        LocalDateTime remindAt = req.eventAt().minusMinutes(
                req.leadMinutes() != null ? req.leadMinutes() : 0
        );

        Reminder reminder = Reminder.builder()
                .user(user)
                .title(req.title())
                .description(req.description())
                .eventAt(req.eventAt())
                .remindAt(remindAt)
                .leadMinutes(req.leadMinutes() != null ? req.leadMinutes() : 0)
                .recurrence(req.recurrence() != null ? req.recurrence() : Reminder.RecurrenceType.NONE)
                .status(Reminder.ReminderStatus.PENDING)
                .build();

        return reminderRepository.save(reminder);
    }

    public List<Reminder> getAllReminders(String userEmail) {
        User user = getUserByEmail(userEmail);
        return reminderRepository.findByUserIdOrderByRemindAtAsc(user.getId());
    }

    public List<Reminder> getUpcomingReminders(String userEmail) {
        User user = getUserByEmail(userEmail);
        return reminderRepository.findUpcomingByUserId(user.getId(), LocalDateTime.now());
    }

    public Reminder updateReminder(String userEmail, Long reminderId, ReminderRequest req) {
        Reminder reminder = getReminderForUser(userEmail, reminderId);

        LocalDateTime remindAt = req.eventAt().minusMinutes(
                req.leadMinutes() != null ? req.leadMinutes() : 0
        );

        reminder.setTitle(req.title());
        reminder.setDescription(req.description());
        reminder.setEventAt(req.eventAt());
        reminder.setRemindAt(remindAt);
        reminder.setLeadMinutes(req.leadMinutes() != null ? req.leadMinutes() : 0);
        reminder.setRecurrence(req.recurrence() != null ? req.recurrence() : Reminder.RecurrenceType.NONE);
        reminder.setStatus(Reminder.ReminderStatus.PENDING);

        return reminderRepository.save(reminder);
    }

    public void deleteReminder(String userEmail, Long reminderId) {
        Reminder reminder = getReminderForUser(userEmail, reminderId);
        reminderRepository.delete(reminder);
    }

    public Reminder dismissReminder(String userEmail, Long reminderId) {
        Reminder reminder = getReminderForUser(userEmail, reminderId);
        reminder.setStatus(Reminder.ReminderStatus.DISMISSED);
        return reminderRepository.save(reminder);
    }

    // ── helpers ──

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private Reminder getReminderForUser(String userEmail, Long reminderId) {
        User user = getUserByEmail(userEmail);
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found: " + reminderId));
        if (!reminder.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return reminder;
    }
}
