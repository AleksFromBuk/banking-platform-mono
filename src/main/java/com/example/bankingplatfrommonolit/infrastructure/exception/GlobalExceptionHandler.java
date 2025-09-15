package com.example.bankingplatfrommonolit.infrastructure.exception;

import com.example.bankingplatfrommonolit.infrastructure.exception.errors.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> nf(NotFoundException e, HttpServletRequest r) {
        return ResponseEntity.status(404).body(err(404, "Not Found", e, r));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> cf(ConflictException e, HttpServletRequest r) {
        return ResponseEntity.status(409).body(err(409, "Conflict", e, r));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> fb(ForbiddenException e, HttpServletRequest r) {
        return ResponseEntity.status(403).body(err(403, "Forbidden", e, r));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> un(UnauthorizedException e, HttpServletRequest r) {
        return ResponseEntity.status(401).body(err(401, "Unauthorized", e, r));
    }

    //-----

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> bad(MethodArgumentNotValidException e, HttpServletRequest r) {
        var details = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining("; "));
        log.warn("400 Validation failed at {} -> {}", r.getRequestURI(), details);
        return ResponseEntity.badRequest().body(err(400, "Bad Request", new RuntimeException(details), r));
    }

    @ExceptionHandler(org.springframework.web.bind.MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> missingHeader(org.springframework.web.bind.MissingRequestHeaderException e, HttpServletRequest r) {
        log.warn("400 Missing header at {} -> {}", r.getRequestURI(), e.getMessage());
        return ResponseEntity.badRequest().body(err(400, "Bad Request", e, r));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException e, HttpServletRequest r) {
        log.warn("400 Body unreadable at {} -> {}", r.getRequestURI(), e.getMostSpecificCause().getMessage());
        return ResponseEntity.badRequest().body(err(400, "Bad Request", e, r));
    }


    //-----


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> any(Exception e, HttpServletRequest r) {
        return ResponseEntity.status(500).body(err(500, "Server Error", e, r));
    }

    private ErrorResponse err(int s, String t, Exception e, HttpServletRequest r) {
        return new ErrorResponse(Instant.now(), s, t, e.getMessage(), r.getRequestURI());
    }
}