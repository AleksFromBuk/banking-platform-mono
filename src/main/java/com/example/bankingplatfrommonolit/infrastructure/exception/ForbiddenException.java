package com.example.bankingplatfrommonolit.infrastructure.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String m) {
        super(m);
    }
}