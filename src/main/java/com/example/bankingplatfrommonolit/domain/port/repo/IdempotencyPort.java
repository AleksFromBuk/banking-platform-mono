package com.example.bankingplatfrommonolit.domain.port.repo;

import java.util.Optional;

public interface IdempotencyPort {
    enum State{IN_PROGRESS, COMPLETED}
    boolean tryStart(String key);
    void complete(String key, String responseJson);
    Optional<String> getCompletedResponse(String ke);
    void cleanupOlderThanDays(int days);
}
