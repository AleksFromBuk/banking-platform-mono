package com.example.bankingplatfrommonolit.Integration;

import com.example.bankingplatfrommonolit.application.dto.card.CardDtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class AdminFlowIT extends BaseIntegrationTest {

    String adminToken;
    UUID userId;

    @BeforeEach
    void boot() {
        adminToken = login("admin", "admin");
        var userTok = registerOrLogin("bob", "bob@example.com", "secret123");
        userId = userIdFromToken(userTok);
    }

    @Test
    void admin_can_create_block_activate_and_filter_cards() {
        HttpHeaders hAdmin = bearer(adminToken);

        var c1 = createCard(http, hAdmin, userId,
                "4242424242424242", LocalDate.now().plusYears(3));
        var c2 = createCard(http, hAdmin, userId,
                "4012888888881881", LocalDate.now().plusYears(2));

        setStatus(hAdmin, c1.id(), "BLOCKED");

        var all = listAdminCards(hAdmin, 100);
        assertNotNull(all);
        assertTrue(Arrays.stream(all).anyMatch(v -> v.id().equals(c1.id())));

        setStatus(hAdmin, c1.id(), "ACTIVE");

        var byAdminFilter = http.exchange("/admin/cards/filter?expiryDateTo=" +
                        LocalDate.now().plusYears(2),
                HttpMethod.GET, new org.springframework.http.HttpEntity<>(hAdmin),
                CardDtos.CardView[].class).getBody();

        assertNotNull(byAdminFilter);
        assertTrue(Arrays.stream(byAdminFilter).anyMatch(v -> v.id().equals(c2.id())));
    }
}
