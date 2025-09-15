package com.example.bankingplatfrommonolit.domain.port.repo;

import com.example.bankingplatfrommonolit.domain.type.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record CardFilter(
        CardStatus status,
        Set<CardStatus> statuses,
        LocalDate expiryDateFrom,
        LocalDate expiryDateTo,
        String last4Digits,
        BigDecimal minBalance,
        BigDecimal maxBalance
) {
    public CardFilter(CardStatus status, LocalDate expiryDateFrom,
                      LocalDate expiryDateTo, String last4Digits) {
        this(status, null, expiryDateFrom, expiryDateTo, last4Digits, null, null);
    }
}