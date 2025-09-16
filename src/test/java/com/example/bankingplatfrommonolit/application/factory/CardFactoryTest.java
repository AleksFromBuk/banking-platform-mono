package com.example.bankingplatfrommonolit.application.factory;

import com.example.bankingplatfrommonolit.application.crypto.PanEncryptor;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CardFactoryUnitTest {

    @Test
    void shouldCreateCardWithMaskedPan() {
        String b64Key = Base64.getEncoder().encodeToString(new byte[32]);
        PanEncryptor encryptor = new PanEncryptor(b64Key);
        CardFactory factory = new CardFactory(encryptor);

        UUID ownerId = UUID.randomUUID();
        String pan = "4111111111111111";
        LocalDate expiry = LocalDate.now().plusYears(3);

        var card = factory.create(ownerId, pan, expiry);

        assertNotNull(card.getId());
        assertEquals(ownerId, card.getOwnerId());
        assertEquals("1111", card.getLast4());
        assertEquals(expiry, card.getExpiryDate());
    }
}