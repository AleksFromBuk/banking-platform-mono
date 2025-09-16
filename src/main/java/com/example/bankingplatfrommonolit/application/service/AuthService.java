package com.example.bankingplatfrommonolit.application.service;

import com.example.bankingplatfrommonolit.application.dto.auth.AuthDtos;
import com.example.bankingplatfrommonolit.domain.model.User;
import com.example.bankingplatfrommonolit.domain.port.repo.RefreshTokenPort;
import com.example.bankingplatfrommonolit.domain.port.repo.UserRepositoryPort;
import com.example.bankingplatfrommonolit.domain.type.Role;
import com.example.bankingplatfrommonolit.infrastructure.exception.ConflictException;
import com.example.bankingplatfrommonolit.infrastructure.exception.ForbiddenException;
import com.example.bankingplatfrommonolit.infrastructure.exception.NotFoundException;
import com.example.bankingplatfrommonolit.infrastructure.exception.UnauthorizedException;
import com.example.bankingplatfrommonolit.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepositoryPort users;
    private final RefreshTokenPort refresh;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @Transactional
    public AuthDtos.TokenResponse register(AuthDtos.RegisterRequest request) {
        if (users.existsByUsernameOrEmail(request.username(), request.email())) {
            throw new ConflictException("User exists");
        }
        var u = User.builder()
                .id(UUID.randomUUID())
                .username(request.username())
                .email(request.email())
                .passwordHash(encoder.encode(request.password()))
                .role(Role.USER)
                .active(true)
                .tokenVersion(0)
                .createdAt(Instant.now())
                .updatedAt(null)
                .build();
        users.save(u);
        return issuePair(u);
    }

    @Transactional
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest r, String ua, String ip) {
        var u = users.findByUsernameOrEmail(r.login())
                .orElseThrow(() -> new UnauthorizedException("Bad credentials"));

        if (!encoder.matches(r.password(), u.getPasswordHash())) {
            throw new UnauthorizedException("Bad credentials");
        }

        if (!u.isActive()) {
            throw new ForbiddenException("User inactive");
        }

        return issuePair(u, ua, ip);
    }

    @Transactional(readOnly = true)
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest r) {
        return login(r, null, null);
    }

    private AuthDtos.TokenResponse issuePair(User user) {
        return issuePair(user, null, null);
    }

    private AuthDtos.TokenResponse issuePair(User user, String ua, String ip) {
        var at = jwt.issueAccess(user.getId(), user.getRole().name(), user.getUsername(), user.getTokenVersion());
        var rtRaw = UUID.randomUUID().toString();

        refresh.save(
                user.getId(),
                sha256(rtRaw),
                Instant.now().plusSeconds(jwt.getRefreshTtlSeconds()),
                ua,
                ip,
                null
        );

        long exp = jwt.parse(at).getBody().getExpiration().toInstant().getEpochSecond();
        log.info("auth: issued tokens for user={}", user.getId());
        return new AuthDtos.TokenResponse(at, rtRaw, exp);
    }


    @Transactional
    public AuthDtos.TokenResponse refresh(String oldRaw) {
        return refresh (oldRaw, null, null);
    }


    @Transactional
    public AuthDtos.TokenResponse refresh(String oldRaw, String ua, String ip) {
        var storedOpt = refresh.findByHash(sha256(oldRaw));
        if (storedOpt.isEmpty()) throw new UnauthorizedException("refresh invalid");
        var stored = storedOpt.get();
        var now = Instant.now();

        if (stored.revoked() || stored.expiresAt().isBefore(now)) {
            users.findById(stored.userId()).ifPresent(u ->
                    users.updateTokenVersion(u.getId(), u.getTokenVersion() + 1));
            refresh.revokeAllByUser(stored.userId());              // ← подчистка
            throw new UnauthorizedException("refresh reused or expired");
        }

        refresh.revoke(stored.id());                                // ← ротация
        var u = users.findById(stored.userId()).orElseThrow(() -> new NotFoundException("user not found"));

        var at = jwt.issueAccess(u.getId(), u.getRole().name(), u.getUsername(), u.getTokenVersion());
        var newRt = UUID.randomUUID().toString();

        refresh.save(u.getId(), sha256(newRt),
                Instant.now().plusSeconds(jwt.getRefreshTtlSeconds()),
                ua, ip, stored.id());

        long exp = jwt.parse(at).getBody().getExpiration().toInstant().getEpochSecond();
        return new AuthDtos.TokenResponse(at, newRt, exp);
    }


    @Transactional
    public void logoutAll(UUID userId) {
        var u = users.findById(userId).orElseThrow(() -> new NotFoundException("user"));
        users.updateTokenVersion(userId, u.getTokenVersion() + 1);
        refresh.revokeAllByUser(userId);
        log.warn("security: logoutAll user={}", userId);
    }

    private static String sha256(String raw) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(md.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}