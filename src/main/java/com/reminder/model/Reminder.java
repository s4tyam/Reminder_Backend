package com.reminder.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reminders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // The actual time the reminder event happens
    @Column(name = "event_at", nullable = false)
    private LocalDateTime eventAt;

    // When to actually fire the notification (event_at minus lead time)
    @Column(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    // Lead time in minutes (e.g. 10 = remind 10 min before)
    @Column(name = "lead_minutes")
    @Builder.Default
    private Integer leadMinutes = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReminderStatus status = ReminderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RecurrenceType recurrence = RecurrenceType.NONE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "reminder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;

    public enum ReminderStatus {
        PENDING, SENT, DISMISSED, SNOOZED
    }

    public enum RecurrenceType {
        NONE, DAILY, WEEKLY, MONTHLY
    }
}
