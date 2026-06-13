package com.reminder.controller;

import com.reminder.model.Reminder;
import com.reminder.service.ReminderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    // GET /api/reminders  — all reminders for current user
    @GetMapping
    public ResponseEntity<List<Reminder>> getAll(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reminderService.getAllReminders(user.getUsername()));
    }

    // GET /api/reminders/upcoming  — only future pending reminders
    @GetMapping("/upcoming")
    public ResponseEntity<List<Reminder>> getUpcoming(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reminderService.getUpcomingReminders(user.getUsername()));
    }

    // POST /api/reminders
    @PostMapping
    public ResponseEntity<Reminder> create(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody @Valid ReminderBody body) {

        ReminderService.ReminderRequest req = new ReminderService.ReminderRequest(
                body.title, body.description, body.eventAt, body.leadMinutes, body.recurrence);
        return ResponseEntity.ok(reminderService.createReminder(user.getUsername(), req));
    }

    // PUT /api/reminders/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Reminder> update(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id,
            @RequestBody @Valid ReminderBody body) {

        ReminderService.ReminderRequest req = new ReminderService.ReminderRequest(
                body.title, body.description, body.eventAt, body.leadMinutes, body.recurrence);
        return ResponseEntity.ok(reminderService.updateReminder(user.getUsername(), id, req));
    }

    // DELETE /api/reminders/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        reminderService.deleteReminder(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/reminders/{id}/dismiss
    @PostMapping("/{id}/dismiss")
    public ResponseEntity<Reminder> dismiss(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        return ResponseEntity.ok(reminderService.dismissReminder(user.getUsername(), id));
    }

    record ReminderBody(
        @NotBlank String title,
        String description,
        @NotNull LocalDateTime eventAt,
        @Min(0) Integer leadMinutes,
        Reminder.RecurrenceType recurrence
    ) {}
}
