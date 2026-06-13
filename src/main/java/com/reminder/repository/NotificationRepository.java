package com.reminder.repository;

import com.reminder.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndIsReadFalseOrderBySentAtDesc(Long userId);
    List<Notification> findByUserIdOrderBySentAtDesc(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
}
