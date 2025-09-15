package com.example.bankingplatfrommonolit.domain.port.repo;

import com.example.bankingplatfrommonolit.domain.model.Card;
import com.example.bankingplatfrommonolit.domain.type.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepositoryPort {
    Card saveWithEncryptedPan(Card card, String encryptedPan);
    Optional<Card> findById(UUID id);
    Optional<Card> findByIdAndOwnerId(UUID id, UUID ownerId);
    List<Card> findByOwnerId(UUID ownerId, int page, int size);
    List<Card> findAll(int page, int size);
    void updateStatus(UUID cardId, CardStatus status);
    void updateBalances(UUID fromId, BigDecimal newFromBalance, UUID toId, BigDecimal newToBalance);
    List<Card> findByOwnerIdAndFilters(UUID ownerId, CardFilter filter, int page, int size);
    List<Card> findAllWithFilters(CardFilter filter, int page, int size);
    List<Card> findByExpiryDateBefore(LocalDate date);
    void updateBalance(UUID id, BigDecimal newBalance);

}
