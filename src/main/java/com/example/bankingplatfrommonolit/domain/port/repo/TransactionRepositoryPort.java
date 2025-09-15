package com.example.bankingplatfrommonolit.domain.port.repo;

import com.example.bankingplatfrommonolit.domain.model.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);
    List<Transaction> findByCardId(UUID cardId, int page, int size);
    List<Transaction> findByCardIdAndOwnerId(UUID cardId, UUID ownerId, int page, int size);
}
