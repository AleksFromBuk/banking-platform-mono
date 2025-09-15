package com.example.bankingplatfrommonolit.application.dto.transaction;

import com.example.bankingplatfrommonolit.domain.type.TxType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class TransactionDtos {
    public record TransactionResponse(
            UUID id,
            UUID cardId,
            TxType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String description,
            Instant createAt
    ) {}
}
