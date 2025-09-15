//package com.example.bankingplatfrommonolit.Integration;
//
//import com.example.bankingplatfrommonolit.application.dto.auth.AuthDtos;
//import com.example.bankingplatfrommonolit.application.dto.card.CardDtos;
//import com.example.bankingplatfrommonolit.application.dto.transfer.TransferDtos;
//import com.example.bankingplatfrommonolit.domain.model.User;
//import com.example.bankingplatfrommonolit.domain.port.repo.UserRepositoryPort;
//import com.example.bankingplatfrommonolit.domain.type.Role;
//import com.example.bankingplatfrommonolit.infrastructure.security.JwtService;
//import io.jsonwebtoken.Jwts;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.time.Instant;
//import java.time.LocalDate;
//import java.util.Objects;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//
//class TransferFlowIT extends BaseIntegrationTest {
//
//    @Autowired
//    TestRestTemplate http;
//
//    @Autowired
//    UserRepositoryPort users;
//    @Autowired
//    PasswordEncoder encoder;
//    @Autowired
//    JwtService jwt;
//
//    String accessAdmin;
//    String accessUser;
//    UUID userId;
//
//    @BeforeEach
//    void setup() {
//        // 1) гарантируем наличие ADMIN
//        users.findByUsernameOrEmail("admin").orElseGet(() -> {
//            var u = User.builder()
//                    .id(java.util.UUID.randomUUID())
//                    .username("admin")
//                    .email("admin@example.com")
//                    .passwordHash(encoder.encode("admin"))   // тот же encoder, что и в SecurityConfig
//                    .role(Role.ADMIN)
//                    .active(true)
//                    .tokenVersion(0)
//                    .createdAt(Instant.now())
//                    .build();
//            return users.save(u);
//
//        });
//
//        // 2) логинимся админом
//        var loginAdmin = new AuthDtos.LoginRequest("admin", "admin");
//        var r1 = http.postForEntity("/auth/login", loginAdmin, AuthDtos.TokenResponse.class);
//        assertEquals(HttpStatus.OK, r1.getStatusCode());
//        accessAdmin = Objects.requireNonNull(r1.getBody()).accessToken();
//
//        // 3) регаем обычного пользователя
//        var reg = new AuthDtos.RegisterRequest(
//                "alice", "alice@example.com", "secret123");
//        var r2 = http.postForEntity("/auth/register", reg,
//                AuthDtos.TokenResponse.class);
//        assertEquals(HttpStatus.OK, r2.getStatusCode());
//        accessUser = r2.getBody().accessToken();
//
//        // 4) достаём userId из access токена (аудит не важен в тесте)
//        var claims = Jwts.parserBuilder().build().parseClaimsJws(accessUser).getBody();
//        userId = UUID.fromString(claims.getSubject());
//    }
//
//    @Test
//    void admin_creates_cards_and_user_transfers_idempotently_and_balances_change() {
//        var hAdmin = new HttpHeaders();
//        hAdmin.setBearerAuth(accessAdmin);
//
//        // создаём две карты юзеру
//        var c1 = http.exchange("/admin/cards", HttpMethod.POST,
//                new HttpEntity<>(
//                        new CardDtos.CreateCardRequest(
//                                userId, "4000000000000002", LocalDate.now().plusYears(3)
//                        ), hAdmin), CardDtos.CardView.class).getBody();
//
//        var c2 = http.exchange("/admin/cards", HttpMethod.POST,
//                new HttpEntity<>(
//                        new CardDtos.CreateCardRequest(
//                                userId, "4000000000000010", LocalDate.now().plusYears(3)
//                        ), hAdmin),
//                CardDtos.CardView.class).getBody();
//
//        // пополняем обе
//        var cv1 = http.exchange("/admin/cards/" + c1.id() + "/topup?amount=100.00",
//               HttpMethod.POST, new HttpEntity<>(hAdmin),
//                CardDtos.CardView.class).getBody();
//
//        var cv2 = http.exchange("/admin/cards/" + c2.id() + "/topup?amount=50.00",
//                HttpMethod.POST, new HttpEntity<>(hAdmin),
//                CardDtos.CardView.class).getBody();
//
//        // делаем перевод 10.00 с идемпотентным ключом
//        var hUser = new HttpHeaders();
//        hUser.setBearerAuth(accessUser);
//        hUser.add("Idempotency-Key", "idem-xyz");
//
//        var req = new TransferDtos.TransferRequest(
//                c1.id(), c2.id(), new java.math.BigDecimal("10.00"), "idem-001");
//
//        var r1 = http.exchange("/transactions/transfer", org.springframework.http.HttpMethod.POST,
//                new HttpEntity<>(req, hUser),
//                TransferDtos.TransferResponse.class);
//        assertEquals(org.springframework.http.HttpStatus.OK, r1.getStatusCode());
//
//        // повтор — должен вернуть тот же ответ (идемпотентность)
//        var r2 = http.exchange("/transactions/transfer", HttpMethod.POST,
//                new HttpEntity<>(req, hUser),
//                TransferDtos.TransferResponse.class);
//        assertEquals(HttpStatus.OK, r2.getStatusCode());
//        assertEquals(r1.getBody().fromBalance(), r2.getBody().fromBalance());
//
//        // --- проверяем изменения балансов (дергаем /admin/cards чтобы не лезть напрямую в БД)
//        var after1 = http.exchange("/admin/cards?size=100", HttpMethod.GET,
//                new HttpEntity<>(hAdmin),
//                CardDtos.CardView[].class).getBody();
//
//        var card1 = java.util.Arrays.stream(after1).filter(v -> v.id().equals(c1.id())).findFirst().orElseThrow();
//        var card2 = java.util.Arrays.stream(after1).filter(v -> v.id().equals(c2.id())).findFirst().orElseThrow();
//
//        assertEquals(new java.math.BigDecimal("90.00"), card1.balance());
//        assertEquals(new java.math.BigDecimal("60.00"), card2.balance());
//    }
//}
//
package com.example.bankingplatfrommonolit.Integration;

import com.example.bankingplatfrommonolit.application.dto.auth.AuthDtos;
import com.example.bankingplatfrommonolit.application.dto.card.CardDtos;
import com.example.bankingplatfrommonolit.application.dto.transfer.TransferDtos;
import com.example.bankingplatfrommonolit.domain.model.User;
import com.example.bankingplatfrommonolit.domain.port.repo.UserRepositoryPort;
import com.example.bankingplatfrommonolit.domain.type.Role;
import com.example.bankingplatfrommonolit.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferFlowIT extends BaseIntegrationTest {

    @Autowired TestRestTemplate http;
    @Autowired UserRepositoryPort users;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;
    @Autowired JwtService jwt;

    String accessAdmin;
    String accessUser;
    UUID userId;

    @BeforeEach
    void setup() {
        RestTemplate rt = http.getRestTemplate();
        rt.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        rt.getInterceptors().add((request, body, execution) -> {
            System.out.println(">>> OUT " + request.getMethod() + " " + request.getURI());
            request.getHeaders().forEach((k, v) -> {
                if ("Authorization".equalsIgnoreCase(k)) {
                    System.out.println(">>> H Authorization: ***redacted***");
                } else {
                    System.out.println(">>> H " + k + ": " + v);
                }
            });
            System.out.println(">>> BODY " + (body == null ? "null" : new String(body, java.nio.charset.StandardCharsets.UTF_8)));

            ClientHttpResponse response = execution.execute(request, body);

            // благодаря BufferingClientHttpRequestFactory это безопасно —
            // тело можно читать тут и потом ещё раз внутри RestTemplate.
            byte[] bytes = org.springframework.util.StreamUtils.copyToByteArray(response.getBody());
            System.out.println("<<< STATUS " + response.getStatusCode());
            System.out.println("<<< BODY " + new String(bytes, java.nio.charset.StandardCharsets.UTF_8));

            // Возвращаем исходный response — он уже буферизирован фабрикой.
            return response;
        });


        //--------------------
        // 1) гарантируем наличие ADMIN (кодек тот же, что и в SecurityConfig)
        users.findByUsernameOrEmail("admin").orElseGet(() -> {
            var u = User.builder()
                    .id(UUID.randomUUID())
                    .username("admin")
                    .email("admin@example.com")
                    .passwordHash(encoder.encode("admin"))
                    .role(Role.ADMIN)
                    .active(true)
                    .tokenVersion(0)
                    .createdAt(Instant.now())
                    .build();
            return users.save(u);
        });

        // 2) логинимся админом
        var loginAdmin = new AuthDtos.LoginRequest("admin", "admin");
        var r1 = http.postForEntity("/auth/login", loginAdmin, AuthDtos.TokenResponse.class);
        assertEquals(HttpStatus.OK, r1.getStatusCode());
        accessAdmin = Objects.requireNonNull(r1.getBody()).accessToken();

        // 3) регистрируем обычного пользователя
        var reg = new AuthDtos.RegisterRequest("alice", "alice@example.com", "secret123");
        var r2 = http.postForEntity("/auth/register", reg, AuthDtos.TokenResponse.class);
        assertEquals(HttpStatus.OK, r2.getStatusCode());
        accessUser = Objects.requireNonNull(r2.getBody()).accessToken();

        // 4) достаем userId из access токена через JwtService (с проверкой подписи)
        var claims = jwt.parse(accessUser).getBody();
        userId = UUID.fromString(claims.getSubject());
    }

    @Test
    void admin_creates_cards_and_user_transfers_idempotently_and_balances_change() {
        var hAdmin = new HttpHeaders();
        hAdmin.setBearerAuth(accessAdmin);

        // создаём две карты юзеру
        var c1 = http.exchange(
                "/admin/cards", HttpMethod.POST,
                new HttpEntity<>(new CardDtos.CreateCardRequest(
                        userId, "4000000000000002", LocalDate.now().plusYears(3)
                ), hAdmin),
                CardDtos.CardView.class
        ).getBody();

        var c2 = http.exchange(
                "/admin/cards", HttpMethod.POST,
                new HttpEntity<>(new CardDtos.CreateCardRequest(
                        userId, "4000000000000010", LocalDate.now().plusYears(3)
                ), hAdmin),
                CardDtos.CardView.class
        ).getBody();

        // пополняем обе
        http.exchange("/admin/cards/" + c1.id() + "/topup?amount=100.00",
                HttpMethod.POST, new HttpEntity<>(hAdmin), CardDtos.CardView.class);

        http.exchange("/admin/cards/" + c2.id() + "/topup?amount=50.00",
                HttpMethod.POST, new HttpEntity<>(hAdmin), CardDtos.CardView.class);

        // делаем перевод 10.00 с идемпотентным ключом (в заголовке)
        var hUser = new HttpHeaders();
        hUser.setBearerAuth(accessUser);
        hUser.add("Idempotency-Key", "idem-xyz");
        hUser.setContentType(MediaType.APPLICATION_JSON);
        hUser.setAccept(List.of(MediaType.APPLICATION_JSON));

        var req = new TransferDtos.TransferRequest(
                c1.id(), c2.id(), new BigDecimal("10.00")
        );

        var r1 = http.exchange("/transactions/transfer", HttpMethod.POST,
                new HttpEntity<>(req, hUser),
                TransferDtos.TransferResponse.class);
        assertEquals(HttpStatus.OK, r1.getStatusCode());

        // повтор — должен вернуть тот же ответ (идемпотентность)
        var r2 = http.exchange("/transactions/transfer", HttpMethod.POST,
                new HttpEntity<>(req, hUser),
                TransferDtos.TransferResponse.class);
        assertEquals(HttpStatus.OK, r2.getStatusCode());
        assertEquals(r1.getBody().fromBalance(), r2.getBody().fromBalance());

        // — проверяем фактические балансы через /admin/cards
        var after = http.exchange("/admin/cards?size=100", HttpMethod.GET,
                new HttpEntity<>(hAdmin), CardDtos.CardView[].class).getBody();

        var card1 = Arrays.stream(after).filter(v -> v.id().equals(c1.id())).findFirst().orElseThrow();
        var card2 = Arrays.stream(after).filter(v -> v.id().equals(c2.id())).findFirst().orElseThrow();

        assertEquals(new java.math.BigDecimal("90.00"), card1.balance());
        assertEquals(new java.math.BigDecimal("60.00"), card2.balance());
    }
}
