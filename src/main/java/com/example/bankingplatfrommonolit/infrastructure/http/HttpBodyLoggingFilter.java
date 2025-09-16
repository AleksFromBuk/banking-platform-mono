package com.example.bankingplatfrommonolit.infrastructure.http;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnProperty(value = "http.bodylog.enabled", havingValue = "true", matchIfMissing = false)
public class HttpBodyLoggingFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Логируем только "наши" JSON вызовы (можно сузить по пути)
        String uri = request.getRequestURI();
        return !(uri.startsWith("/transactions/transfer") || uri.startsWith("/admin/cards"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wreq = new ContentCachingRequestWrapper(req);
        ContentCachingResponseWrapper wres = new ContentCachingResponseWrapper(res);

        try {
            chain.doFilter(wreq, wres);
        } finally {
            String inBody = new String(wreq.getContentAsByteArray(), StandardCharsets.UTF_8);
            String outBody = new String(wres.getContentAsByteArray(), StandardCharsets.UTF_8);

            log.info(">>> IN {} {} headers={} body={}",
                    req.getMethod(), req.getRequestURI(), safeHeaders(req), truncate(inBody));

            log.info("<<< OUT {} {} status={} body={}",
                    req.getMethod(), req.getRequestURI(), res.getStatus(), truncate(outBody));

            wres.copyBodyToResponse();
        }
    }

    private static String truncate(String s) {
        if (s == null) return "null";
        return s.length() > 2000 ? s.substring(0, 2000) + "...(truncated)" : s;
    }

    private static String safeHeaders(HttpServletRequest req) {
        var map = Collections.list(req.getHeaderNames()).stream()
                .collect(java.util.stream.Collectors.toMap(
                        h -> h,
                        h -> h.equalsIgnoreCase("authorization") ? "***redacted***" : req.getHeader(h)
                ));
        return map.toString();
    }
}
