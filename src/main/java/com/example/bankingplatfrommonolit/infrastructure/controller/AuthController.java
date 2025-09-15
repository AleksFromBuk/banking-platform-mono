package com.example.bankingplatfrommonolit.infrastructure.controller;

import com.example.bankingplatfrommonolit.application.dto.auth.AuthDtos;
import com.example.bankingplatfrommonolit.application.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/register")
    public ResponseEntity<AuthDtos.TokenResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest r) {
        return ResponseEntity.ok(auth.register(r));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.TokenResponse> login(@Valid @RequestBody AuthDtos.LoginRequest r, HttpServletRequest req) {
        return ResponseEntity.ok(auth.login(r, req.getHeader("User-Agent"), req.getRemoteAddr()));
    }

    @PostMapping("/refresh")
    public AuthDtos.TokenResponse refresh(
            @RequestBody AuthDtos.RefreshRequest body,
            @RequestHeader(value = "User-Agent", required = false) String ua,
            HttpServletRequest req
    ) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = req.getRemoteAddr();
        String refreshRaw = Objects.requireNonNull(body.refreshToken(), "refreshToken is required");
        return auth.refresh(refreshRaw, ua != null ? ua : "unknown", ip);
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@RequestBody AuthDtos.LogoutAllRequest r) {
        auth.logoutAll(r.userId());
        return ResponseEntity.noContent().build();
    }
}
