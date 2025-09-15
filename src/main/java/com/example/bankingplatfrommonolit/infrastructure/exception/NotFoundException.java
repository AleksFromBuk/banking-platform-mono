package com.example.bankingplatfrommonolit.infrastructure.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String m) {
        super(m);
    }
}