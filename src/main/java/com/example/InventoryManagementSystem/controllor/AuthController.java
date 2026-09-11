package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.config.JwtUtil;
import com.example.InventoryManagementSystem.config.TokenBlacklistService;
import com.example.InventoryManagementSystem.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.InventoryManagementSystem.dto.LoginRequest;
import com.example.InventoryManagementSystem.dto.RegisterRequest;
import com.example.InventoryManagementSystem.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklist;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(
                authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request));
    }

    /**
     * Revoke the presented bearer token so it can no longer be used, even
     * though it is still within its expiry window (stateless-JWT logout).
     * Always returns 200 — logging out is not allowed to fail.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            tokenBlacklist.revoke(token, jwtUtil.getExpiry(token));
        }
        return ResponseEntity.ok("Logged out");
    }
}