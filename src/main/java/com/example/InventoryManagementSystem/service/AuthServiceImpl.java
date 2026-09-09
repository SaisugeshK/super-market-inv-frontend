package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.config.JwtUtil;
import com.example.InventoryManagementSystem.dto.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.InventoryManagementSystem.dto.LoginRequest;
import com.example.InventoryManagementSystem.dto.RegisterRequest;
import com.example.InventoryManagementSystem.model.Role;
import com.example.InventoryManagementSystem.model.User;
import com.example.InventoryManagementSystem.Repository.RoleRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // Self-service registration always creates the lowest-privilege role.
    // Elevated accounts are created by an ADMIN through /api/users.
    public static final String DEFAULT_SELF_SIGNUP_ROLE = "CASHIER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public String register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        Integer roleId = roleRepository.findByRoleName(DEFAULT_SELF_SIGNUP_ROLE)
                .map(Role::getRoleId)
                .orElse(null);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roleId(roleId)
                .active(Boolean.TRUE)
                .status("ACTIVE")
                .build();

        userRepository.save(user);

        return "User Registered Successfully";
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String roleName = user.getRoleId() == null ? null
                : roleRepository.findById(user.getRoleId())
                        .map(Role::getRoleName)
                        .orElse(null);

        String token = jwtUtil.generateToken(user.getEmail(), roleName);

        return AuthResponse.builder()
                .token(token)
                .message("Login Successful")
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleName(roleName)
                .build();
    }
}
