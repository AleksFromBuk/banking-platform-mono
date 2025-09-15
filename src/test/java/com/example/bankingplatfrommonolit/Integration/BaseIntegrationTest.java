package com.example.bankingplatfrommonolit.Integration;


import com.example.bankingplatfrommonolit.BankingPlatfromMonolitApplication;
import com.example.bankingplatfrommonolit.application.dto.auth.AuthDtos;
import com.example.bankingplatfrommonolit.application.dto.card.CardDtos;
import com.example.bankingplatfrommonolit.infrastructure.security.JwtService;
import com.example.bankingplatfrommonolit.Integration.config.TestDataSeeder;
import com.example.bankingplatfrommonolit.Integration.config.TestHttpClientConfig;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest(classes = BankingPlatfromMonolitApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestHttpClientConfig.class, TestDataSeeder.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

    // Testcontainers — стартуем ЗАРАНЕЕ
    static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bankdb")
            .withUsername("app")
            .withPassword("app")
            .withReuse(true);

    static {
        PG.start();
    }

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", PG::getJdbcUrl);
        r.add("spring.datasource.username", PG::getUsername);
        r.add("spring.datasource.password", PG::getPassword);
    }

    @Autowired
    protected TestRestTemplate http;
    @Autowired
    protected JwtService jwt;

    // ---------- AUTH ----------
    protected String login(String username, String password) {
        var resp = http.postForEntity("/auth/login",
                new AuthDtos.LoginRequest(username, password),
                AuthDtos.TokenResponse.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException("login failed for " + username + ": " + resp.getStatusCode());
        }
        return resp.getBody().accessToken();
    }

    /**
     * register или login, если юзер уже есть (409)
     */
    protected String registerOrLogin(String username, String email, String password) {
        ResponseEntity<AuthDtos.TokenResponse> resp;
        try {
            resp = http.postForEntity(
                    "/auth/register",
                    new AuthDtos.RegisterRequest(username, email, password),
                    AuthDtos.TokenResponse.class
            );
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // на всякий случай: если где-то остался дефолтный ErrorHandler и он кидает исключение
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                return login(username, password);
            }
            throw e;
        }

        // обычный путь: RestTemplate не кинул исключение, но статус может быть 409
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            return resp.getBody().accessToken();
        }
        if (resp.getStatusCode() == HttpStatus.CONFLICT) {
            return login(username, password);
        }

        throw new IllegalStateException("register failed: " + resp.getStatusCode());
    }

    protected UUID userIdFromToken(String accessToken) {
        var claims = jwt.parse(accessToken).getBody();
        return UUID.fromString(claims.getSubject());
    }

    protected HttpHeaders bearer(String token) {
        var h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setAccept(List.of(MediaType.APPLICATION_JSON));
        return h;
    }

    // ---------- CARDS ----------
    protected CardDtos.CardView createCard(TestRestTemplate http, HttpHeaders adminHeaders,
                                           UUID ownerId, String pan, LocalDate expiry) {
        var req = new CardDtos.CreateCardRequest(ownerId, pan, expiry);
        var resp = http.exchange("/admin/cards", HttpMethod.POST,
                new HttpEntity<>(req, adminHeaders), CardDtos.CardView.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException("createCard failed: " + resp.getStatusCode());
        }
        return resp.getBody();
    }

    protected CardDtos.CardView[] listAdminCards(HttpHeaders adminHeaders, int size) {
        return http.exchange("/admin/cards?size=" + size, HttpMethod.GET,
                new HttpEntity<>(adminHeaders), CardDtos.CardView[].class).getBody();
    }

    protected void setStatus(HttpHeaders adminHeaders, UUID cardId, String status) {
        http.exchange("/admin/cards/{id}/status?status={s}", HttpMethod.PATCH,
                new HttpEntity<>(adminHeaders), Void.class, cardId, status);
    }

    protected CardDtos.CardView topUp(HttpHeaders adminHeaders, UUID cardId, BigDecimal amount) {
        return http.exchange("/admin/cards/{id}/topup?amount={a}", HttpMethod.POST,
                new HttpEntity<>(adminHeaders), CardDtos.CardView.class,
                cardId, amount.toPlainString()).getBody();
    }

    // ---------- PAN / LUHN ----------
    protected static int luhnCheckDigit(String first15) {
        int sum = 0;
        for (int i = first15.length() - 1, pos = 0; i >= 0; i--, pos++) {
            int d = first15.charAt(i) - '0';
            if (pos % 2 == 0) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * PAN c заданными последними 4 цифрами (последняя — лун-чек)
     */
    protected static String panWithDesiredLast4(String last4) {
        if (last4 == null || !last4.matches("\\d{4}")) {
            throw new IllegalArgumentException("last4 must be 4 digits");
        }
        String last3 = last4.substring(0, 3);
        int targetCheck = last4.charAt(3) - '0';

        char[] base12 = "400000000000".toCharArray(); // dev BIN
        for (int pos = 0; pos < base12.length; pos++) {
            char original = base12[pos];
            for (char d = '0'; d <= '9'; d++) {
                base12[pos] = d;
                String first15 = new String(base12) + last3; // 12+3
                int check = luhnCheckDigit(first15);
                if (check == targetCheck) {
                    return first15 + check;
                }
            }
            base12[pos] = original;
        }
        throw new IllegalStateException("Cannot generate PAN for last4=" + last4);
    }

    /**
     * Последовательный валидный PAN (Luhn), конец не фиксирован
     */
    protected static String genPanSeq(int i) {
        String base15 = "400000000000" + String.format("%03d", Math.floorMod(i, 1000));
        return base15 + luhnCheckDigit(base15);
    }

    /**
     * PAN с заданными ТРЕМЯ последними цифрами; 4-я — вычисленный Luhn-check
     */
    protected static String genPanWithLast3(String last3) {
        if (last3 == null || !last3.matches("\\d{3}"))
            throw new IllegalArgumentException("last3 must be 3 digits");
        String base15 = "400000000000" + last3; // 12 + 3 = 15
        return base15 + luhnCheckDigit(base15);
    }
}
