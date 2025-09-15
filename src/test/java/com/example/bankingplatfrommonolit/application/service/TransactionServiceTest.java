package com.example.bankingplatfrommonolit.application.service;

import com.example.bankingplatfrommonolit.domain.model.Card;
import com.example.bankingplatfrommonolit.domain.port.repo.CardRepositoryPort;
import com.example.bankingplatfrommonolit.domain.port.repo.TransactionRepositoryPort;
import com.example.bankingplatfrommonolit.domain.type.CardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {
    @Mock
    CardRepositoryPort cards;
    @Mock
    TransactionRepositoryPort txs;
    @Mock
    IdempotencyService idem;
    @InjectMocks
    TransferService svc;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void different_owners_forbidden() {
        UUID ownerA = UUID.randomUUID(), ownerB = UUID.randomUUID();
        UUID fromId = UUID.randomUUID(), toId = UUID.randomUUID();

        var from = Card.builder()
                .id(fromId)
                .ownerId(ownerA)
                .last4("1111")
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("100"))
                .version(0)
                .expiryDate(LocalDate.now().plusYears(2))
                .createdAt(Instant.now())
                .build();

        var to = Card.builder()
                .id(toId)
                .ownerId(ownerB)
                .last4("2222")
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("0"))
                .version(0)
                .expiryDate(LocalDate.now().plusYears(2))
                .createdAt(Instant.now())
                .build();

        when(cards.findById(fromId)).thenReturn(Optional.of(from));
        when(cards.findById(toId)).thenReturn(Optional.of(to));
        when(idem.shortCircuitIfCompleted(any())).thenReturn(Optional.empty());
        when(idem.tryStart(any())).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                svc.transfer(ownerA, false, fromId, toId, new BigDecimal("10"), "k1"));

        verify(cards, never()).updateBalances(any(), any(), any(), any());
    }
}
