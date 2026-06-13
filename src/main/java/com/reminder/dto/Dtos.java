package com.reminder.dto;

import com.reminder.model.Reminder;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

// ───── Auth DTOs ─────

class RegisterRequest {
    @NotBlank @Email
    public String email;
    @NotBlank @Size(min = 6)
    public String password;
    @NotBlank
    public String fullName;
    public String phoneNumber;
}

class LoginRequest {
    @NotBlank @Email
    public String email;
    @NotBlank
    public String password;
}

class AuthResponse {
    public String token;
    public String email;
    public String fullName;
    public Long userId;

    public AuthResponse(String token, String email, String fullName, Long userId) {
        this.token = token; this.email = email;
        this.fullName = fullName; this.userId = userId;
    }
}

// ───── Reminder DTOs ─────

class ReminderRequest {
    @NotBlank
    public String title;
    public String description;

    @NotNull
    public LocalDateTime eventAt;      // when the actual event happens

    @NotNull @Min(0)
    public Integer leadMinutes;        // remind X minutes before eventAt

    public Reminder.RecurrenceType recurrence;
}

class ReminderResponse {
    public Long id;
    public String title;
    public String description;
    public LocalDateTime eventAt;
    public LocalDateTime remindAt;
    public Integer leadMinutes;
    public Reminder.ReminderStatus status;
    public Reminder.RecurrenceType recurrence;
    public LocalDateTime createdAt;
}

// ───── Notification DTOs ─────

class NotificationResponse {
    public Long id;
    public Long reminderId;
    public String reminderTitle;
    public String message;
    public String type;
    public Boolean isRead;
    public LocalDateTime sentAt;
}

// ───── Push Subscription ─────

class PushSubscriptionRequest {
    public String endpoint;
    public PushKeys keys;

    @Getter @Setter
    public static class PushKeys {
        public String p256dh;
        public String auth;
    }
}
