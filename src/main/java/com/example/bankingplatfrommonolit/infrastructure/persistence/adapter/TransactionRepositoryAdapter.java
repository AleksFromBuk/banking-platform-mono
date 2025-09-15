package com.example.bankingplatfrommonolit.infrastructure.persistence.adapter;

import com.example.bankingplatfrommonolit.domain.model.Transaction;
import com.example.bankingplatfrommonolit.domain.port.repo.TransactionRepositoryPort;
import com.example.bankingplatfrommonolit.infrastructure.mapper.TransactionMapper;
import com.example.bankingplatfrommonolit.infrastructure.persistence.jpa.TransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {
    private final TransactionJpaRepository jpa;
    private final TransactionMapper mapper;

    @Override
    public Transaction save(Transaction t) {
        return mapper.toDomain(jpa.save(mapper.toEntity(t)));
    }

    @Override
    public List<Transaction> findByCardId(UUID cardId, int page, int size) {
        return jpa.findByCardId(cardId, PageRequest.of(page, size)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByCardIdAndOwnerId(UUID cardId, UUID ownerId, int page, int size) {
        return jpa.findByCardIdAndOwnerId(cardId, ownerId, PageRequest.of(page, size)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}

