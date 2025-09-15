package com.example.bankingplatfrommonolit.Integration;

import com.example.bankingplatfrommonolit.application.dto.transfer.TransferDtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransfersIT extends BaseIntegrationTest {

    String adminToken;
    String userToken;
    UUID userId;

    @BeforeEach
    void setup() {
        adminToken = login("admin", "admin");
        userToken  = registerOrLogin("alice", "alice@example.com", "secret123");
        userId = userIdFromToken(userToken);
    }

    @Test
    void happy_path_and_idempotency() {
        var hAdmin = bearer(adminToken);
        var c1 = createCard(http, hAdmin, userId, "4242424242424242", LocalDate.now().plusYears(3));
        var c2 = createCard(http, hAdmin, userId, "4012888888881881", LocalDate.now().plusYears(3));

        topUp(hAdmin, c1.id(), new BigDecimal("100.00"));
        topUp(hAdmin, c2.id(), new BigDecimal("50.00"));

        var hUser = bearer(userToken);
        hUser.add("Idempotency-Key", "idem-xyz");
        var req = new TransferDtos.TransferRequest(c1.id(), c2.id(), new BigDecimal("10.00"));

        var r1 = http.exchange("/transactions/transfer", HttpMethod.POST,
                new HttpEntity<>(req, hUser), TransferDtos.TransferResponse.class);
        assertEquals(HttpStatus.OK, r1.getStatusCode());

        var r2 = http.exchange("/transactions/transfer", HttpMethod.POST,
                new HttpEntity<>(req, hUser), TransferDtos.TransferResponse.class);
        assertEquals(HttpStatus.OK, r2.getStatusCode());
        assertEquals(r1.getBody().fromBalance(), r2.getBody().fromBalance());

        var after = listAdminCards(hAdmin, 100);
        var a1 = Arrays.stream(after).filter(v -> v.id().equals(c1.id())).findFirst().orElseThrow();
        var a2 = Arrays.stream(after).filter(v -> v.id().equals(c2.id())).findFirst().orElseThrow();
        assertEquals(new BigDecimal("90.00"), a1.balance());
        assertEquals(new BigDecimal("60.00"), a2.balance());
    }

    @Test
    void cannot_transfer_from_or_to_foreign_card() {
        var hAdmin = bearer(adminToken);
        var meTok  = registerOrLogin("u1", "u1@example.com", "secret123");
        var himTok = registerOrLogin("u2", "u2@example.com", "secret123");
        var me  = userIdFromToken(meTok);
        var him = userIdFromToken(himTok);

        var cMine = createCard(http, hAdmin, me,  "4000056655665556", LocalDate.now().plusYears(3));
        var cHis  = createCard(http, hAdmin, him, "4111111111111111", LocalDate.now().plusYears(3));
        topUp(hAdmin, cMine.id(), new BigDecimal("100.00"));

        var hUser = bearer(meTok);
        hUser.add("Idempotency-Key", "k1");
        var req = new TransferDtos.TransferRequest(cMine.id(), cHis.id(), new BigDecimal("5.00"));

        var r = http.exchange("/transactions/transfer", HttpMethod.POST, new HttpEntity<>(req, hUser), String.class);
        assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
    }

    @Test
    void insufficient_funds() {
        var hAdmin = bearer(adminToken);
        var c1 = createCard(http, hAdmin, userId, "5200828282828210", LocalDate.now().plusYears(3)); // MC test
        var c2 = createCard(http, hAdmin, userId, "4000002500003155", LocalDate.now().plusYears(3)); // Visa test

        var hUser = bearer(userToken);
        hUser.add("Idempotency-Key", "k2");
        var req = new TransferDtos.TransferRequest(c1.id(), c2.id(), new BigDecimal("1.00"));

        var r = http.exchange("/transactions/transfer", HttpMethod.POST, new HttpEntity<>(req, hUser), String.class);
        assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
    }
}
