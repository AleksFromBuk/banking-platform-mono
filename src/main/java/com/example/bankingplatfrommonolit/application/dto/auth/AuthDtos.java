package com.example.bankingplatfrommonolit.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class AuthDtos {
    public record RegisterRequest(
            @NotBlank String username,
            @Email @NotBlank String email,
            @Size(min = 6) String password
    ){}

    public record LoginRequest(
            @NotBlank String login,
            @NotBlank String password
    ) {}

    public static record RefreshRequest(String refreshToken) {}

    public record TokenResponse(
            String accessToken,
            String refreshToken,
            long accessExpEpochSec
    ) {}

    public record LogoutAllRequest(@NotNull UUID userId) {}
}
