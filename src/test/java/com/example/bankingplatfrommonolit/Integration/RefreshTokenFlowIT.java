package com.example.bankingplatfrommonolit.Integration;

import com.example.bankingplatfrommonolit.application.dto.auth.AuthDtos;
import com.example.bankingplatfrommonolit.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenFlowIT extends BaseIntegrationTest {

    @Autowired TestRestTemplate http;
    @Autowired JwtService jwt;

    String access1;
    String refresh1;
    UUID userId;

    @BeforeEach
    void boot() {
        var t = registerOrLoginFull("frank", "frank@example.com", "secret123");
        access1  = t.accessToken();
        refresh1 = t.refreshToken();
        userId   = userIdFromToken(access1);
        assertNotNull(refresh1);
    }

    @Test
    void refresh_rotates_and_old_refresh_is_rejected() {
        // удачный refresh
        var h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("User-Agent", "it-tests");
        var ok = http.postForEntity("/auth/refresh",
                new HttpEntity<>(new AuthDtos.RefreshRequest(refresh1), h),
                AuthDtos.TokenResponse.class);
        assertEquals(HttpStatus.OK, ok.getStatusCode());
        var t2 = ok.getBody();
        assertNotNull(t2);
        assertNotEquals(access1,  t2.accessToken());
        assertNotEquals(refresh1, t2.refreshToken());
        assertEquals(userId, UUID.fromString(jwt.parse(t2.accessToken()).getBody().getSubject()));

        // повторное использование старого refresh — должно быть не 200
        var bad = http.postForEntity("/auth/refresh",
                new HttpEntity<>(new AuthDtos.RefreshRequest(refresh1), h),
                String.class);
        assertNotEquals(HttpStatus.OK, bad.getStatusCode());
    }

    private AuthDtos.TokenResponse registerOrLoginFull(String u, String e, String p) {
        var reg = http.postForEntity("/auth/register",
                new AuthDtos.RegisterRequest(u, e, p), AuthDtos.TokenResponse.class);
        if (reg.getStatusCode().is2xxSuccessful()) return reg.getBody();

        var login = http.postForEntity("/auth/login",
                new AuthDtos.LoginRequest(u, p), AuthDtos.TokenResponse.class);
        assertEquals(HttpStatus.OK, login.getStatusCode(), "login failed after register conflict");
        return login.getBody();
    }
}
