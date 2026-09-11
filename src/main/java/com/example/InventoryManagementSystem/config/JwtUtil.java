package com.example.InventoryManagementSystem.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret:mySecretKeymySecretKeymySecretKey123456}") String secret,
            @Value("${jwt.expiration:86400000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public String generateToken(String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // unique per token, so revoking one never affects another
                .subject(email)
                .claim("role", role == null ? "" : role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    /**
     * Returns the token claims if it is well-formed, correctly signed and
     * unexpired; otherwise {@code null}.
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    public String validateAndGetSubject(String token) {
        Claims c = parse(token);
        return c == null ? null : c.getSubject();
    }

    /** Expiry of a valid token, or {@code null} if it can't be parsed. */
    public Instant getExpiry(String token) {
        Claims c = parse(token);
        return c == null || c.getExpiration() == null ? null : c.getExpiration().toInstant();
    }
}
