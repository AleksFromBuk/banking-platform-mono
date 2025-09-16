package com.example.bankingplatfrommonolit.application.validation;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;

@UtilityClass
public class CardValidators {
    public static boolean luhn(String pan) {
        int s = 0; boolean alt = false;
        for (int i = pan.length() - 1; i >= 0; i--) {
            int d = pan.charAt(i) - '0';
            if (alt) { d *= 2; if (d > 9) d -= 9; }
            s += d; alt = !alt;
        }
        return s % 10 == 0;
    }

    public static void requireFuture(LocalDate expiry) {
        if (expiry.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Card expired");
        }
    }
}
