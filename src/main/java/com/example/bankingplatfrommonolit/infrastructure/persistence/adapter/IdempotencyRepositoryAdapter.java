package com.example.bankingplatfrommonolit.infrastructure.persistence.adapter;

import com.example.bankingplatfrommonolit.domain.port.repo.IdempotencyPort;
import com.example.bankingplatfrommonolit.infrastructure.persistence.entity.IdempotencyKeyEntity;
import com.example.bankingplatfrommonolit.infrastructure.persistence.jpa.IdempotencyKeyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IdempotencyRepositoryAdapter implements IdempotencyPort {
    private final IdempotencyKeyJpaRepository jpa;

    @Override
    @Transactional
    public boolean tryStart(String key) {
        if (key == null) return true;

        var e = new IdempotencyKeyEntity();
        e.setId(key);
        e.setStatus("IN_PROGRESS");
        e.setCreatedAt(Instant.now());

        try {
            jpa.save(e);
            return true;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }

    @Override
    @Transactional
    public void complete(String key, String json) {
        var e = jpa.findById(key).orElseThrow();
        e.setStatus("COMPLETED");
        e.setResponseJson(json);
        jpa.save(e);
    }

    @Override
    public Optional<String> getCompletedResponse(String key) {
        return key == null ? Optional.empty() :
                jpa.findById(key)
                        .filter(x -> "COMPLETED".equals(x.getStatus()))
                        .map(IdempotencyKeyEntity::getResponseJson);
    }

    @Override
    @Transactional
    public int cleanupOlderThanDays(int days) {
        return jpa.cleanup(Instant.now().minusSeconds(days * 24L * 3600));
    }
}