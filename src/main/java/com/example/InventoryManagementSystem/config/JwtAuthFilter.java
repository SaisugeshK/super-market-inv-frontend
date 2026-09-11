package com.example.InventoryManagementSystem.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads a Bearer token, validates it, and — if valid — marks the request as
 * authenticated with the user's role as a {@code ROLE_*} authority. Invalid or
 * missing tokens are left unauthenticated; the security chain then rejects
 * protected endpoints with 401.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklist;

    public JwtAuthFilter(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklist) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String rawToken = header.substring(7).trim();
            Claims claims = jwtUtil.parse(rawToken);

            if (claims != null && claims.getSubject() != null
                    && !tokenBlacklist.isRevoked(rawToken)) {
                String role = claims.get("role", String.class);
                List<SimpleGrantedAuthority> authorities = (role == null || role.isBlank())
                        ? List.of()
                        : List.of(new SimpleGrantedAuthority("ROLE_" + role));

                var auth = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
