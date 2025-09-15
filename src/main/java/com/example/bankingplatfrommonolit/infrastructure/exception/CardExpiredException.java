package com.example.bankingplatfrommonolit.infrastructure.exception;

import java.util.UUID;

public class CardExpiredException extends ConflictException {
    public CardExpiredException(UUID cardId) {
        super("Card with id " + cardId + " has expired");
    }
}