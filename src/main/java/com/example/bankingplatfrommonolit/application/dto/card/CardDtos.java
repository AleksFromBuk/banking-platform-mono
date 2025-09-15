package com.example.bankingplatfrommonolit.application.dto.card;

import com.example.bankingplatfrommonolit.domain.type.CardStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class CardDtos {
    public record CreateCardRequest(
            @NotNull UUID ownerId,
            @Pattern(regexp = "\\d{16}") String pan,
            @NotNull LocalDate expiry
    ) {}

    public record CardView(
            UUID id,
            String maskedPan,
            String last4,
            LocalDate expiryDate,
            String status,
            BigDecimal balance,
            UUID ownerId
    ) {}

    public record CardFilterRequest(
            CardStatus status,
            LocalDate expiryDateFrom,
            LocalDate expiryDateTo,
            String last4Digits

    ) {}
}
