package com.example.bankingplatfrommonolit.application.validation;

public final class BusinessValidators {
    private BusinessValidators() {}

    public static void require(boolean condition, String msg) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }
}
