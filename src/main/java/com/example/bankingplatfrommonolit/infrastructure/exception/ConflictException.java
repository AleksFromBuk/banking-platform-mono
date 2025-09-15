package com.example.bankingplatfrommonolit.infrastructure.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String m) {
        super(m);
    }
}