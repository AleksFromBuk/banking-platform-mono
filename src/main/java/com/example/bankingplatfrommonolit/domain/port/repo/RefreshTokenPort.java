package com.example.bankingplatfrommonolit.domain.port.repo;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenPort {
    record Stored(long id, UUID userId, String tokenHash, Instant expiresAt, boolean revoked, Long rotatedFrom) {}
    void save (UUID userId, String tokenHash, Instant expiresAt, String ua, String ip, Long rotatedFrom);
    Optional<Stored> findByHash(String tokenHash);
    void revoke(long id);
    int revokeAllByUser(UUID id);
    int cleanupExpiredRevoked(Instant cutoff);
}
