package com.example.bankingplatfrommonolit.Integration;

import com.example.bankingplatfrommonolit.application.dto.card.CardDtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CardMaskingIT extends BaseIntegrationTest {

    @Autowired TestRestTemplate http;

    String adminToken;
    String userToken;
    UUID userId;

    @BeforeEach
    void boot() {
        adminToken = login("admin", "admin");
        userToken  = registerOrLogin("dave", "dave@example.com", "secret123");
        userId     = userIdFromToken(userToken);
    }

    @Test
    void masked_pan_is_returned_and_full_pan_never_leaks_in_json() {
        var hAdmin = bearer(adminToken);
        var hUser  = bearer(userToken);

        String pan = genPanSeq(100);
        var card = createCard(http, hAdmin, userId, pan, LocalDate.now().plusYears(2));

        // 1) в объектной модели есть maskedPan / panMasked и last4
        var mine = http.exchange("/cards?size=100", HttpMethod.GET, new HttpEntity<>(hUser), CardDtos.CardView[].class).getBody();
        assertNotNull(mine);
        var same = Arrays.stream(mine).filter(v -> v.id().equals(card.id())).findFirst().orElseThrow();

        String expectedMask = "**** **** **** " + same.last4();
        // допускаем разные имена геттера для маски (maskedPan / panMasked)
        try {
            var m = same.getClass().getMethod("maskedPan");
            String actual = (String) m.invoke(same);
            assertEquals(expectedMask, actual);
        } catch (NoSuchMethodException e) {
            try {
                var m = same.getClass().getMethod("panMasked");
                String actual = (String) m.invoke(same);
                assertEquals(expectedMask, actual);
            } catch (Exception ex) {
                fail("CardView не содержит maskedPan/panMasked");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        // 2) в JSON не должно быть полного PAN
        var json = http.exchange("/cards?size=100", HttpMethod.GET, new HttpEntity<>(hUser), String.class).getBody();
        assertNotNull(json);
        assertTrue(json.contains(same.last4()));   // хвост видим
        assertFalse(json.contains(pan));           // полный PAN не светится
    }
}

