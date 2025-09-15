package com.example.bankingplatfrommonolit.infrastructure.exception.errors;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {}
