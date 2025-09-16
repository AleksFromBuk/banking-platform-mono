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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferServiceUnitTest {

    @Mock
    private CardRepositoryPort cards;
    @Mock
    private TransactionRepositoryPort transactions;
    @Mock
    private IdempotencyService idempotency;

    @InjectMocks
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldThrowExceptionWhenTransferBetweenDifferentOwners() {
        UUID ownerA = UUID.randomUUID();
        UUID ownerB = UUID.randomUUID();
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Card from = Card.builder()
                .id(fromId)
                .ownerId(ownerA)
                .last4("1111")
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .version(0L)
                .build();

        Card to = Card.builder()
                .id(toId)
                .ownerId(ownerB)
                .last4("2222")
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("50.00"))
                .version(0L)
                .build();

        when(cards.findById(fromId)).thenReturn(Optional.of(from));
        when(cards.findById(toId)).thenReturn(Optional.of(to));
        when(idempotency.shortCircuitIfCompleted(any())).thenReturn(Optional.empty());
        when(idempotency.tryStart(any())).thenReturn(true);

        assertThrows(Exception.class, () ->
                transferService.transfer(ownerA, false, fromId, toId, new BigDecimal("10.00"), "key"));

        verify(cards, never()).updateBalances(any(), any(), any(), any());
    }
}