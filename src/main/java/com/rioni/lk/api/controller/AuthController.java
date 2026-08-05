package com.rioni.lk.api.controller;

import com.rioni.lk.api.dto.AuthRequest;
import com.rioni.lk.api.dto.AuthResponse;
import com.rioni.lk.api.dto.ChangePasswordRequest;
import com.rioni.lk.api.dto.CheckContactRequest;
import com.rioni.lk.api.dto.CheckContactResponse;
import com.rioni.lk.api.dto.RecoverSmsRequest;
import com.rioni.lk.api.dto.SmsCodeRequest;
import com.rioni.lk.api.dto.SmsCodeResponse;
import com.rioni.lk.api.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-sms")
    public ResponseEntity<AuthResponse> checkSms(@RequestBody SmsCodeRequest request) {
        AuthResponse response = authService.checkSmsCode(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        Integer profileId = (Integer) authentication.getPrincipal();
        AuthResponse response = authService.refreshToken(profileId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            Integer profileId = (Integer) authentication.getPrincipal();
            authService.logout(profileId);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-contact")
    public ResponseEntity<CheckContactResponse> checkContact(@RequestBody CheckContactRequest request) {
        CheckContactResponse response = authService.checkContact(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recover-sms")
    public ResponseEntity<SmsCodeResponse> recoverSms(@RequestBody RecoverSmsRequest request) {
        SmsCodeResponse response = authService.recoverSms(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change_password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }
}
