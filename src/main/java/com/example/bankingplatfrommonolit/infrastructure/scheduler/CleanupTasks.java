package com.example.bankingplatfrommonolit.infrastructure.scheduler;

import com.example.bankingplatfrommonolit.domain.port.repo.IdempotencyPort;
import com.example.bankingplatfrommonolit.domain.port.repo.RefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
class CleanupTasks {
    private final IdempotencyPort idem;
    private final RefreshTokenPort rt;

    @Scheduled(cron = "0 30 3 * * *")
    public void cleanup() {
        idem.cleanupOlderThanDays(7);
        rt.cleanupExpiredRevoked(Instant.now().minusSeconds(3600));
    }
}