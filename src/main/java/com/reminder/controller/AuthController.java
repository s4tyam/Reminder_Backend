package com.reminder.controller;

import com.reminder.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<AuthService.AuthResponse> register(
            @RequestBody @Valid RegisterBody body) {

        AuthService.RegisterRequest req = new AuthService.RegisterRequest(
                body.email, body.password, body.fullName, body.phoneNumber);
        return ResponseEntity.ok(authService.register(req));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthService.AuthResponse> login(
            @RequestBody @Valid LoginBody body) {

        AuthService.LoginRequest req = new AuthService.LoginRequest(body.email, body.password);
        return ResponseEntity.ok(authService.login(req));
    }

    // Request bodies as inner records
    record RegisterBody(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        @NotBlank String fullName,
        String phoneNumber
    ) {}

    record LoginBody(
        @NotBlank @Email String email,
        @NotBlank String password
    ) {}
}
