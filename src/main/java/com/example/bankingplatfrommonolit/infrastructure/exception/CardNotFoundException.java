package com.example.bankingplatfrommonolit.infrastructure.exception;

import java.util.UUID;

public class CardNotFoundException extends NotFoundException {
    public CardNotFoundException(UUID cardId) {
        super("Card with id " + cardId + " not found");
    }
}
