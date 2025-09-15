package com.example.bankingplatfrommonolit.application.service;

import com.example.bankingplatfrommonolit.application.dto.card.CardDtos;
import com.example.bankingplatfrommonolit.application.factory.CardFactory;
import com.example.bankingplatfrommonolit.domain.model.Card;
import com.example.bankingplatfrommonolit.domain.port.repo.CardRepositoryPort;
import com.example.bankingplatfrommonolit.domain.type.CardStatus;
import com.example.bankingplatfrommonolit.infrastructure.exception.ConflictException;
import com.example.bankingplatfrommonolit.infrastructure.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardCommandService {
    private final CardRepositoryPort cards;
    private final CardFactory factory;
    private final TransactionService tx;


    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CardDtos.CardView create(CardDtos.CreateCardRequest r) {
        Card c = factory.create(r.ownerId(), r.pan(), r.expiry());
        String encryptedPan = factory.encryptPan(r.pan());        // как у тебя
        var saved = cards.saveWithEncryptedPan(c, encryptedPan);
        log.info("card:create id={} owner={}", saved.getId(), saved.getOwnerId());
        return toView(saved);
    }

    @Transactional
    public void requestBlock(UUID requesterId, UUID cardId) {
        var c = cards.findByIdAndOwnerId(cardId, requesterId)
                .orElseThrow(() -> new NotFoundException("card"));

        if (c.getStatus() != CardStatus.ACTIVE) {
            throw new ConflictException("Only ACTIVE can be block-requested");
        }

        cards.updateStatus(cardId, CardStatus.BLOCK_REQUESTED);
        log.warn("card:block-request id={} owner={}", cardId, requesterId);
    }

    @Transactional
    public void changeStatusAsAdmin(UUID cardId, CardStatus status) {
        cards.findById(cardId).orElseThrow(() -> new NotFoundException("card"));
        cards.updateStatus(cardId, status);
        log.warn("card:status admin id={} status={}", cardId, status);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CardDtos.CardView topUp(UUID cardId, java.math.BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ConflictException("amount must be > 0");
        }
        var c = cards.findById(cardId).orElseThrow(() -> new NotFoundException("card"));
        if (c.getStatus() != CardStatus.ACTIVE) {
            throw new ConflictException("Card not ACTIVE");
        }
        var newBalance = c.getBalance().add(amount);
        cards.updateBalance(cardId, newBalance);
        tx.recordTopUp(cardId, amount, newBalance);
        var after = cards.findById(cardId).orElseThrow();
        return toView(after);
    }

    static CardDtos.CardView toView(Card c) {
        return new CardDtos.CardView(
                c.getId(),
                mask(c.getLast4()),
                c.getLast4(),
                c.getExpiryDate(),
                c.getStatus().name(),
                c.getBalance(),
                c.getOwnerId()
        );
    }
    static String mask(String last4) { return "**** **** **** " + last4; }
}