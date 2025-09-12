package com.example.bankingplatfrommonolit.domain.model;

import com.example.bankingplatfrommonolit.domain.type.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Card {
    private final UUID id;
    private final UUID ownerId;
    private final String last4;
    private final LocalDate expiryDate;
    private final CardStatus status;
    private final BigDecimal balance;
    private final long version;
    private final Instant createdAt;
}
