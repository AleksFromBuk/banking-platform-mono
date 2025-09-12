package com.example.bankingplatfrommonolit.domain.model;

import com.example.bankingplatfrommonolit.domain.type.TxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Transaction {
    private final UUID id;
    private final UUID cardId;
    private final TxType type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final String description;
    private final Instant createdAt;
}
