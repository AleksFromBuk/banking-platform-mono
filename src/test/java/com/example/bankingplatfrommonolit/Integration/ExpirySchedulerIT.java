package com.example.bankingplatfrommonolit.Integration;

import com.example.bankingplatfrommonolit.application.dto.card.CardDtos;
import com.example.bankingplatfrommonolit.infrastructure.scheduler.CardExpirationTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExpirySchedulerIT extends BaseIntegrationTest {

    @Autowired
    TestRestTemplate http;
    @Autowired
    DataSource dataSource;

    @Autowired
    CardExpirationTask cardExpirationTask;

    String adminToken;
    String userToken;
    UUID userId;

    @BeforeEach
    void boot() {
        adminToken = login("admin", "admin");
        userToken  = registerOrLogin("eve", "eve@example.com", "secret123");
        userId     = userIdFromToken(userToken);
    }

    @Test
    void scheduler_marks_expired_cards_as_EXPIRED() throws Exception {
        HttpHeaders hAdmin = bearer(adminToken);

        // создаём 4 карты пользователю
        var c1 = createCard(http, hAdmin, userId, genPanSeq(0), LocalDate.now().plusYears(1));
        var c2 = createCard(http, hAdmin, userId, genPanSeq(1), LocalDate.now().plusYears(2));
        var c3 = createCard(http, hAdmin, userId, genPanSeq(2), LocalDate.now().plusYears(3));
        var c4 = createCard(http, hAdmin, userId, genPanSeq(3), LocalDate.now().plusYears(1));

        // баланс на одной из активных (для уверенности, что баланс не влияет на expiry)
        topUp(hAdmin, c2.id(), new BigDecimal("10.00"));

        // делаем c1 и c3 "истёкшими" напрямую в БД
        setExpiryDateRaw(c1.id(), LocalDate.now().minusDays(1));
        setExpiryDateRaw(c3.id(), LocalDate.now().minusDays(30));

        cardExpirationTask.checkExpiredCards();

        // проверяем статусы через /admin/cards
        var after = http.exchange("/admin/cards?size=500", HttpMethod.GET,
                new HttpEntity<>(hAdmin), CardDtos.CardView[].class).getBody();
        assertNotNull(after);

        Map<UUID,String> statuses = Arrays.stream(after)
                .filter(v -> Set.of(c1.id(), c2.id(), c3.id(), c4.id()).contains(v.id()))
                .collect(Collectors.toMap(CardDtos.CardView::id, CardDtos.CardView::status));

        assertEquals("EXPIRED", statuses.get(c1.id()), "c1 должен стать EXPIRED");
        assertEquals("EXPIRED", statuses.get(c3.id()), "c3 должен стать EXPIRED");
        assertEquals("ACTIVE",  statuses.get(c2.id()), "c2 остаётся ACTIVE");
        assertEquals("ACTIVE",  statuses.get(c4.id()), "c4 остаётся ACTIVE");
    }

    /** Быстрый апдейт expiry_date в БД с явным коммитом */
    private void setExpiryDateRaw(UUID cardId, LocalDate newDate) throws Exception {
        Connection c = DataSourceUtils.getConnection(dataSource);
        boolean prevAutoCommit = c.getAutoCommit();
        try {
            // гарантируем автокоммит, чтобы изменение увидел следующий вызов сервиса
            c.setAutoCommit(true);
            try (PreparedStatement ps =
                         c.prepareStatement("update cards set expiry_date=? where id=?")) {
                ps.setObject(1, newDate);
                ps.setObject(2, cardId);
                int upd = ps.executeUpdate();
                if (upd != 1) throw new IllegalStateException("No card updated: " + cardId);
            }
        } finally {
            try { c.setAutoCommit(prevAutoCommit); } catch (Exception ignore) {}
            DataSourceUtils.releaseConnection(c, dataSource);
        }
    }
}