package com.example.bankingplatfrommonolit.domain.model;

import com.example.bankingplatfrommonolit.domain.type.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class User {
    private final UUID id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final boolean active;
    private final int tokenVersion;
    private final Instant createdAt;
    private final Instant updatedAt;

}
