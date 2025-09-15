package com.example.bankingplatfrommonolit.application.dto.transfer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public final class TransferDtos {

    public record TransferRequest(
            @NotNull @JsonProperty("fromCardId") UUID fromCardId,
            @NotNull @JsonProperty("toCardId") UUID toCardId,
            @NotNull
            @DecimalMin(value = "0.01", message = "Amount must be at least \"0.01\" ")
            @Digits(integer = 10, fraction = 2, message = "Amount must have up to 2 decimal places")
            @JsonProperty("amount") BigDecimal amount
    ) {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public TransferRequest {}
    }

    public record TransferResponse(
            UUID fromCardId,
            UUID toCardId,
            BigDecimal fromBalance,
            BigDecimal toBalance,
            String status
    ) {}
}