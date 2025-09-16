package com.example.bankingplatfrommonolit.application.factory;

import com.example.bankingplatfrommonolit.application.crypto.PanEncryptor;
import com.example.bankingplatfrommonolit.application.validation.CardValidators;
import com.example.bankingplatfrommonolit.domain.model.Card;
import com.example.bankingplatfrommonolit.domain.type.CardStatus;
import com.example.bankingplatfrommonolit.infrastructure.exception.CardInvalidPanCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardFactory {
    private final PanEncryptor encryptor;

    public Card create(UUID ownerId, String rawPan, LocalDate expiry) {
        if (!CardValidators.luhn(rawPan)) throw new CardInvalidPanCodeException(rawPan);
        CardValidators.requireFuture(expiry);

        String last4 = rawPan.substring(rawPan.length() - 4);
        return Card.builder()
                .id(UUID.randomUUID())
                .ownerId(ownerId)
                .last4(last4)
                .expiryDate(expiry)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .version(0L)
                .createdAt(Instant.now())
                .build();
    }

    public String encryptPan(String rawPan) {
        return encryptor.encrypt(rawPan);
    }

    public String decryptPan(String encryptedPan) {
        return encryptor.decrypt(encryptedPan);
    }
}
