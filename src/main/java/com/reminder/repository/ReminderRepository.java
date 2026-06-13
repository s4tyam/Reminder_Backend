package com.reminder.repository;

import com.reminder.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUserIdOrderByRemindAtAsc(Long userId);

    // Used by scheduler: find all PENDING reminders due in the next window
    @Query("SELECT r FROM Reminder r WHERE r.status = 'PENDING' AND r.remindAt <= :now")
    List<Reminder> findDueReminders(@Param("now") LocalDateTime now);

    // Upcoming reminders for a user (future + pending)
    @Query("SELECT r FROM Reminder r WHERE r.user.id = :userId AND r.remindAt > :now AND r.status = 'PENDING' ORDER BY r.remindAt ASC")
    List<Reminder> findUpcomingByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
