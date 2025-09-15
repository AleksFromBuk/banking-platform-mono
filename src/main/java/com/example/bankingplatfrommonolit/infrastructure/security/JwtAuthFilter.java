package com.example.bankingplatfrommonolit.infrastructure.security;

import com.example.bankingplatfrommonolit.domain.port.repo.UserRepositoryPort;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements Filter {
    private final JwtService jwt;
    private final UserRepositoryPort users;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
        var r = (HttpServletRequest) req;
        var h = r.getHeader(HttpHeaders.AUTHORIZATION);

        if (h != null && h.startsWith("Bearer ")) {
            try {
                var c = jwt.parse(h.substring(7)).getBody();
                var userId = UUID.fromString(c.getSubject());
                int tokenVersion = c.get("tv", Integer.class);

                var u = users.findById(userId).orElseThrow();
                if (u.getTokenVersion() != tokenVersion) {
                    ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                var role = String.valueOf(c.get("role"));
                var auth = new UsernamePasswordAuthenticationToken(
                        userId.toString(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        fc.doFilter(req, res);
    }
}