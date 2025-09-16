package com.example.bankingplatfrommonolit.infrastructure.bootstrap;

import com.example.bankingplatfrommonolit.domain.model.User;
import com.example.bankingplatfrommonolit.domain.port.repo.UserRepositoryPort;
import com.example.bankingplatfrommonolit.domain.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {
    private final UserRepositoryPort users;
    private final PasswordEncoder encoder;

    @Value("${app.admin.username:admin}") String username;
    @Value("${app.admin.email:admin@example.com}") String email;
    @Value("${app.admin.password:admin}") String password;

    @Override
    public void run(String... args) {
        users.findByUsernameOrEmail(username).orElseGet(() -> {
            var u = User.builder()
                    .id(UUID.randomUUID())
                    .username(username)
                    .email(email)
                    .passwordHash(encoder.encode(password))
                    .role(Role.ADMIN)
                    .active(true)
                    .tokenVersion(0)
                    .createdAt(Instant.now())
                    .build();
            return users.save(u);
        });
    }
}

