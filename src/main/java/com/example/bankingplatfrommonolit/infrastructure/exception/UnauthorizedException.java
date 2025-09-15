package com.example.bankingplatfrommonolit.infrastructure.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String m) {
        super(m);
    }
}
