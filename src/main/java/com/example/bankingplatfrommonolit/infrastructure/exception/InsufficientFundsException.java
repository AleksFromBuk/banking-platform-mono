package com.example.bankingplatfrommonolit.infrastructure.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends ConflictException {
    public InsufficientFundsException(BigDecimal currentBalance, BigDecimal requiredAmount) {
        super("Insufficient funds. Current: " + currentBalance + ", Required: " + requiredAmount);
    }
}
