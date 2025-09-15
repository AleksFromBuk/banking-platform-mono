package com.example.bankingplatfrommonolit.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtService {
    @Value("${jwt.issuer}") private String issuer;
    @Value("${jwt.audience}") private String audience;
    @Value("${jwt.access-ttl-sec:900}") private long accessTtl;
    @Value("${jwt.refresh-ttl-sec:2592000}") private long refreshTtl;
    @Value("${jwt.private-key-location}") private Resource privateKeyRes;
    @Value("${jwt.public-key-location}") private Resource publicKeyRes;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        this.privateKey = Pem.loadPrivate(privateKeyRes);
        this.publicKey = Pem.loadPublic(publicKeyRes);
    }

    public String issueAccess(UUID userId, String role, String username, int tokenVersion) {
        var now = Instant.now();
        var exp = now.plusSeconds(accessTtl);

        return Jwts.builder()
                .setHeaderParam("kid", "k1")
                .setIssuer(issuer)
                .setAudience(audience)
                .setSubject(userId.toString())
                .setId(UUID.randomUUID().toString())
                .addClaims(Map.of("role", role, "username", username, "tv", tokenVersion))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseClaimsJws(token);
    }

    public long getRefreshTtlSeconds() {
        return refreshTtl;
    }

    static class Pem {
        static PrivateKey loadPrivate(Resource r) {
            try (var in = r.getInputStream()) {
                var k = new String(in.readAllBytes());
                var s = k.replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s+", "");
                var pkcs8 = java.util.Base64.getDecoder().decode(s);
                return KeyFactory.getInstance("RSA")
                        .generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(pkcs8));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        static PublicKey loadPublic(Resource r) {
            try (var in = r.getInputStream()) {
                var k = new String(in.readAllBytes());
                var s = k.replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s+", "");
                var x = java.util.Base64.getDecoder().decode(s);
                return KeyFactory.getInstance("RSA")
                        .generatePublic(new java.security.spec.X509EncodedKeySpec(x));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}