package com.example.bankingplatfrommonolit.infrastructure.exception;

import com.example.bankingplatfrommonolit.infrastructure.exception.errors.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.security.Principal;
import java.time.Instant;
import java.util.Optional;

/**
 * Унифицированный глобальный обработчик ошибок.
 * Не логируем чувствительные данные (Authorization, полноразмерный PAN, refresh и т.п.).
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ====== Доменные исключения (твои кастомные) ======

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> unauthorized(UnauthorizedException ex, HttpServletRequest req) {
        return warn(HttpStatus.UNAUTHORIZED, safeMsg(ex), req);
    }
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> forbidden(ForbiddenException ex, HttpServletRequest req) {
        return warn(HttpStatus.FORBIDDEN, safeMsg(ex), req);
    }
    @ExceptionHandler({ NotFoundException.class, CardNotFoundException.class })
    public ResponseEntity<ErrorResponse> notFound(RuntimeException ex, HttpServletRequest req) {
        return info(HttpStatus.NOT_FOUND, safeMsg(ex), req);
    }
    @ExceptionHandler({ ConflictException.class, InsufficientFundsException.class, CardExpiredException.class, CardInvalidPanCodeException.class })
    public ResponseEntity<ErrorResponse> conflict(RuntimeException ex, HttpServletRequest req) {
        // CardExpiredException тут тоже 409 — не ломаем текущие тесты; при желании поменяешь на 422
        return warn(HttpStatus.CONFLICT, safeMsg(ex), req);
    }
    // ====== Валидация / парсинг ======
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> bad(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("Validation failed");
        return info(HttpStatus.BAD_REQUEST, msg, req);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> bad(ConstraintViolationException ex, HttpServletRequest req) {
        var msg = ex.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .orElse("Validation failed");
        return info(HttpStatus.BAD_REQUEST, msg, req);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> bad(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String name = ex.getName();
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "type";
        return info(HttpStatus.BAD_REQUEST, "Parameter '" + name + "' must be " + type, req);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> bad(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return info(HttpStatus.BAD_REQUEST, "Malformed JSON request", req);
    }
    // ====== HTTP-инфраструктура ======
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> methodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                          HttpServletRequest req) {
        return warn(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", req);
    }
    // Требует:
    // spring.mvc.throw-exception-if-no-handler-found=true
    // spring.web.resources.add-mappings=false
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> noHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        return info(HttpStatus.NOT_FOUND, "Not found", req);
    }
    // ====== Security stack ======
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return warn(HttpStatus.FORBIDDEN, "Forbidden", req);
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> auth(AuthenticationException ex, HttpServletRequest req) {
        return warn(HttpStatus.UNAUTHORIZED, "Unauthorized", req);
    }
    // ====== Прозрачная прокси-обработка ======
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> status(ResponseStatusException ex, HttpServletRequest req) {
        var st = HttpStatus.valueOf(ex.getStatusCode().value());
        var msg = Optional.ofNullable(ex.getReason()).orElse(st.getReasonPhrase());
        if (st.is5xxServerError()) return error(st, msg, req, ex);
        if (st == HttpStatus.NOT_FOUND || st == HttpStatus.BAD_REQUEST) return info(st, msg, req);
        return warn(st, msg, req);
    }
    // ====== Fallback ======
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> any(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, ex);
    }
    // ====== helpers ======
    private static String safeMsg(Throwable ex) {
        return Optional.ofNullable(ex.getMessage()).orElse(ex.getClass().getSimpleName());
    }

    private ResponseEntity<ErrorResponse> info(HttpStatus status, String message, HttpServletRequest req) {
        withMdc(req, () -> log.info("[{}] {} {} -> {} {}",
                remote(req), req.getMethod(), req.getRequestURI(), status.value(), message));
        return build(status, message, req);
    }

    private ResponseEntity<ErrorResponse> warn(HttpStatus status, String message, HttpServletRequest req) {
        withMdc(req, () -> log.warn("[{}] {} {} -> {} {}",
                remote(req), req.getMethod(), req.getRequestURI(), status.value(), message));
        return build(status, message, req);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest req, Exception ex) {
        withMdc(req, () -> log.error("[{}] {} {} -> {} {}",
                remote(req), req.getMethod(), req.getRequestURI(), status.value(), message, ex));
        return build(status, message, req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        var body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    private static String remote(HttpServletRequest req) {
        String ua = Optional.ofNullable(req.getHeader("User-Agent")).orElse("-");
        String ip = Optional.ofNullable(req.getHeader("X-Forwarded-For"))
                .orElse(Optional.ofNullable(req.getRemoteAddr()).orElse("-"));
        String user = Optional.ofNullable(req.getUserPrincipal()).map(Principal::getName).orElse("-");
        return "ip=" + ip + " ua=" + (ua.length() > 120 ? ua.substring(0, 117) + "..." : ua) + " user=" + user;
    }

    private static void withMdc(HttpServletRequest req, Runnable r) {
        String rid = req.getHeader("X-Request-ID");
        if (rid == null || rid.isBlank()) { r.run(); return; }
        try (var c = MDC.putCloseable("reqId", rid)) { r.run(); }
    }
}