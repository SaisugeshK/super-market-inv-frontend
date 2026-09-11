package com.example.InventoryManagementSystem.config;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side invalidation for JWTs that are still cryptographically valid but
 * must no longer be accepted — currently: tokens explicitly revoked at logout.
 *
 * The map holds a SHA-256 of the token (never the raw token) against the
 * token's own expiry; entries are evicted lazily on lookup and opportunistically
 * on insert, so the map can never grow past the number of tokens issued within
 * one jwt.expiration window. In-memory is sufficient for a single-instance
 * deployment; a multi-node setup would back this with Redis.
 */
@Service
public class TokenBlacklistService {

    private final Map<String, Instant> revoked = new ConcurrentHashMap<>();

    public void revoke(String token, Instant expiresAt) {
        if (token == null || token.isBlank() || expiresAt == null) {
            return;
        }
        purgeExpired();
        revoked.put(hash(token), expiresAt);
    }

    public boolean isRevoked(String token) {
        if (token == null) {
            return false;
        }
        Instant exp = revoked.get(hash(token));
        if (exp == null) {
            return false;
        }
        if (exp.isBefore(Instant.now())) {
            revoked.remove(hash(token));
            return false;
        }
        return true;
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        revoked.entrySet().removeIf(e -> e.getValue().isBefore(now));
    }

    private static String hash(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(token.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed present on every JVM; fall back to identity.
            return token;
        }
    }
}
