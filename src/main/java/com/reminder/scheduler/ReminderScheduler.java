package com.reminder.scheduler;

import com.reminder.model.Reminder;
import com.reminder.repository.ReminderRepository;
import com.reminder.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ReminderRepository reminderRepository;
    private final NotificationService notificationService;

    // Runs every 60 seconds
    @Scheduled(fixedRateString = "${app.scheduler.interval}")
    public void checkDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> dueReminders = reminderRepository.findDueReminders(now);

        if (!dueReminders.isEmpty()) {
            log.info("Scheduler: {} reminder(s) due at {}", dueReminders.size(), now);
        }

        for (Reminder reminder : dueReminders) {
            try {
                // Send all notification types
                notificationService.sendReminderNotifications(reminder);

                // Handle recurrence — schedule next occurrence
                if (reminder.getRecurrence() != Reminder.RecurrenceType.NONE) {
                    rescheduleRecurring(reminder);
                } else {
                    reminder.setStatus(Reminder.ReminderStatus.SENT);
                    reminderRepository.save(reminder);
                }

                log.info("Reminder '{}' fired for user {}",
                        reminder.getTitle(), reminder.getUser().getEmail());

            } catch (Exception e) {
                log.error("Failed to process reminder {}: {}", reminder.getId(), e.getMessage());
            }
        }
    }

    private void rescheduleRecurring(Reminder reminder) {
        LocalDateTime nextEventAt = switch (reminder.getRecurrence()) {
            case DAILY   -> reminder.getEventAt().plusDays(1);
            case WEEKLY  -> reminder.getEventAt().plusWeeks(1);
            case MONTHLY -> reminder.getEventAt().plusMonths(1);
            default      -> null;
        };

        if (nextEventAt != null) {
            reminder.setEventAt(nextEventAt);
            reminder.setRemindAt(nextEventAt.minusMinutes(reminder.getLeadMinutes()));
            reminder.setStatus(Reminder.ReminderStatus.PENDING);
            reminderRepository.save(reminder);
            log.info("Recurring reminder '{}' rescheduled to {}", reminder.getTitle(), nextEventAt);
        }
    }
}
