package com.example.bankingplatfrommonolit.Integration;

import com.example.bankingplatfrommonolit.application.dto.card.CardDtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CardsSearchAndPaginationIT extends BaseIntegrationTest {

    @Autowired TestRestTemplate http;

    String adminToken;
    String userToken;
    UUID userId;

    @BeforeEach
    void boot() {
        adminToken = login("admin", "admin");
        userToken  = registerOrLogin("charlie", "charlie@example.com", "secret123");
        userId     = userIdFromToken(userToken);
    }

    @Test
    void filters_pagination_and_bulk_block_zero_balances() {
        var hAdmin = bearer(adminToken);
        var hUser  = bearer(userToken);

        final int N = 23;
        List<CardDtos.CardView> created = new ArrayList<>(N);

        for (int i = 0; i < N; i++) {
            String pan   = genPanSeq(i); // Luhn-валидная 16-значная
            LocalDate ex = LocalDate.now().plusYears(1 + (i % 3));
            created.add(createCard(http, hAdmin, userId, pan, ex));
        }
        assertEquals(N, created.size());

        // пополняем три карты
        var cRich1 = created.get(5);
        var cRich2 = created.get(7);
        var cRich3 = created.get(12);
        topUp(hAdmin, cRich1.id(), new BigDecimal("150.00"));
        topUp(hAdmin, cRich2.id(), new BigDecimal("200.00"));
        topUp(hAdmin, cRich3.id(), new BigDecimal("350.00"));

        // --- пагинация /cards
        var p0 = http.exchange("/cards?page=0&size=10", HttpMethod.GET, new HttpEntity<>(hUser), CardDtos.CardView[].class).getBody();
        var p1 = http.exchange("/cards?page=1&size=10", HttpMethod.GET, new HttpEntity<>(hUser), CardDtos.CardView[].class).getBody();
        var p2 = http.exchange("/cards?page=2&size=10", HttpMethod.GET, new HttpEntity<>(hUser), CardDtos.CardView[].class).getBody();
        assertNotNull(p0); assertNotNull(p1); assertNotNull(p2);
        assertEquals(10, p0.length);
        assertEquals(10, p1.length);
        assertEquals(3,  p2.length);

        var ids = new HashSet<UUID>();
        Arrays.stream(p0).forEach(v -> ids.add(v.id()));
        Arrays.stream(p1).forEach(v -> ids.add(v.id()));
        Arrays.stream(p2).forEach(v -> ids.add(v.id()));
        assertEquals(N, ids.size());

        // --- пользовательский фильтр: minBalance >= 300
        // ВАЖНО: тест "строгий" — упадёт, если контроллер не биндит minBalance!
        var rich = http.exchange("/cards/filter?minBalance=300&size=500", HttpMethod.GET,
                new HttpEntity<>(hUser), CardDtos.CardView[].class).getBody();
        assertNotNull(rich);
        assertTrue(Arrays.stream(rich).anyMatch(v -> v.id().equals(cRich3.id())));
        assertTrue(Arrays.stream(rich).allMatch(v -> v.balance().compareTo(new BigDecimal("300.00")) >= 0));

        // --- фильтр last4
        String last4 = cRich2.last4();
        var byLast4 = http.exchange("/cards/filter?last4Digits=" + last4, HttpMethod.GET,
                new HttpEntity<>(hUser), CardDtos.CardView[].class).getBody();
        assertNotNull(byLast4);
        assertTrue(Arrays.stream(byLast4).allMatch(v -> v.last4().equals(last4)));

        // --- фильтр по сроку действия (до +2 лет)
        var to2y = http.exchange("/cards/filter?expiryDateTo=" + LocalDate.now().plusYears(2),
                HttpMethod.GET, new HttpEntity<>(hUser), CardDtos.CardView[].class).getBody();
        assertNotNull(to2y);
        assertTrue(to2y.length > 0);

        // --- массовая блокировка нулевых балансов (админ)
        var allAdmin = http.exchange("/admin/cards?size=500", HttpMethod.GET,
                new HttpEntity<>(hAdmin), CardDtos.CardView[].class).getBody();
        assertNotNull(allAdmin);

        var zeroIds = Arrays.stream(allAdmin)
                .filter(v -> v.ownerId().equals(userId))
                .filter(v -> v.balance().compareTo(BigDecimal.ZERO) == 0)
                .map(CardDtos.CardView::id)
                .toList();

        for (UUID id : zeroIds) {
            http.exchange("/admin/cards/{id}/status?status=BLOCKED", HttpMethod.PATCH,
                    new HttpEntity<>(hAdmin), Void.class, id);
        }

        var afterBlock = http.exchange("/admin/cards?size=500", HttpMethod.GET,
                new HttpEntity<>(hAdmin), CardDtos.CardView[].class).getBody();
        assertNotNull(afterBlock);
        var blockedNow = Arrays.stream(afterBlock)
                .filter(v -> zeroIds.contains(v.id()))
                .collect(Collectors.toMap(CardDtos.CardView::id, CardDtos.CardView::status));
        assertFalse(blockedNow.isEmpty());
        blockedNow.values().forEach(st -> assertEquals("BLOCKED", st));
    }
}