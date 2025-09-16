package com.example.bankingplatfrommonolit.infrastructure.exception;

public class CardInvalidPanCodeException extends  ConflictException {
    public CardInvalidPanCodeException(String rawPanCode) {
        super("invalid panCode: " + rawPanCode);
    }
}