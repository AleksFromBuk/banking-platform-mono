package com.example.bankingplatfrommonolit.infrastructure.persistence.adapter;

import com.example.bankingplatfrommonolit.domain.port.repo.RefreshTokenPort;
import com.example.bankingplatfrommonolit.infrastructure.persistence.entity.RefreshTokenEntity;
import com.example.bankingplatfrommonolit.infrastructure.persistence.jpa.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenPort {
    private final RefreshTokenJpaRepository jpa;

    @Override
    @Transactional
    public void save(UUID userId, String hash, Instant exp, String ua, String ip, Long rotatedFrom) {
        var e = new RefreshTokenEntity();
        e.setUserId(userId);
        e.setTokenHash(hash);
        e.setExpiresAt(exp);
        e.setUserAgent(ua);
        e.setIp(ip);
        e.setRevoked(false);
        e.setRotatedFrom(rotatedFrom);
        e.setCreatedAt(Instant.now());
        jpa.save(e);
    }

    @Override
    public Optional<Stored> findByHash(String hash) {
        return jpa.findByTokenHash(hash)
                .map(e -> new Stored(e.getId(), e.getUserId(), e.getTokenHash(), e.getExpiresAt(), e.isRevoked(), e.getRotatedFrom()));
    }

    @Override
    @Transactional
    public void revoke(long id) {
        var e = jpa.findById(id).orElseThrow();
        e.setRevoked(true);
        jpa.save(e);
    }

    @Override
    @Transactional
    public int revokeAllByUser(UUID uid) {
        return jpa.revokeAllByUser(uid);
    }

    @Override
    @Transactional
    public int cleanupExpiredRevoked(Instant cutoff) {
        return jpa.cleanup(cutoff);
    }
}
