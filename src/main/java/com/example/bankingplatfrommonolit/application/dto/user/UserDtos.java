package com.example.bankingplatfrommonolit.application.dto.user;

import com.example.bankingplatfrommonolit.domain.type.Role;

import java.util.UUID;

public final class UserDtos {
    public record UserResponse(
            UUID id,
            String username,
            String email,
            boolean active,
            Role role
    ) {}

    public record UserStatusRequest(boolean active) {}
}
