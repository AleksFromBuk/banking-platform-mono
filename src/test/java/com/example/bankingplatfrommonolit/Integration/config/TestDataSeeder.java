package com.example.bankingplatfrommonolit.Integration.config;

import com.example.bankingplatfrommonolit.domain.model.User;
import com.example.bankingplatfrommonolit.domain.port.repo.UserRepositoryPort;
import com.example.bankingplatfrommonolit.domain.type.Role;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

@TestConfiguration
@Profile("test")
public class TestDataSeeder {

    @Bean
    ApplicationRunner seedAdmin(UserRepositoryPort users, PasswordEncoder encoder) {
        return args -> users.findByUsernameOrEmail("admin").orElseGet(() -> {
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
    }
}
