package com.example.bankingplatfrommonolit.application.service;

import com.example.bankingplatfrommonolit.domain.port.repo.IdempotencyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final IdempotencyPort idem;

    public Optional<String> shortCircuitIfCompleted(String key) {
        if (key == null) {
            log.debug("Idempotency key is null, skipping short-circuit");
            return Optional.empty();
        }
        var response = idem.getCompletedResponse(key);
        if (response.isPresent()) {
            log.info("Idempotency short-circuit for key: {}", key);
        }
        return response;
    }

    public boolean tryStart(String key) {
        if (key == null) {
            log.debug("No idempotency key provided, allowing operation");
            return true;
        }
        boolean started = idem.tryStart(key);
        log.debug("Idempotency start attempt for key: {} - result: {}", key, started);
        return started;
    }

    public void complete(String key, String responseJson) {
        if (key != null) {
            log.debug("Completing idempotency for key: {}", key);
            idem.complete(key, responseJson);
            log.info("Idempotency completed for key: {}", key);
        }
    }

    public void cleanupOlderThanDays(int days) {
        try {
            log.info("Starting idempotency cleanup for records older than {} days", days);
            int deletedCount = idem.cleanupOlderThanDays(days);
            log.info("Idempotency cleanup completed, removed {} records", deletedCount);
        } catch (Exception e) {
            log.error("Failed to cleanup idempotency records", e);
        }
    }
}