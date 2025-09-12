package com.example.bankingplatfrommonolit.domain.port.repo;

import com.example.bankingplatfrommonolit.domain.type.CardStatus;

import java.time.LocalDate;

public record CardFilter(
        CardStatus status,
        LocalDate expiryDateFrom,
        LocalDate expiryDateTo,
        String last4Digits
) {
}
