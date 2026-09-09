package com.example.InventoryManagementSystem.config;

import com.example.InventoryManagementSystem.model.Role;
import com.example.InventoryManagementSystem.model.User;
import com.example.InventoryManagementSystem.Repository.RoleRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds the fixed role set and a bootstrap ADMIN account on first boot so the
 * system is usable and RBAC has something to enforce. Idempotent.
 */
@Component
public class BootstrapDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(BootstrapDataInitializer.class);

    public static final List<String> ROLES = List.of("ADMIN", "MANAGER", "CASHIER");

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String adminEmail;
    private final String adminUsername;
    private final String adminPassword;

    public BootstrapDataInitializer(RoleRepository roleRepository,
                                    UserRepository userRepository,
                                    PasswordEncoder passwordEncoder,
                                    @Value("${bootstrap.admin.email:admin@erp.local}") String adminEmail,
                                    @Value("${bootstrap.admin.username:admin}") String adminUsername,
                                    @Value("${bootstrap.admin.password:admin123}") String adminPassword) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        for (String name : ROLES) {
            if (!roleRepository.existsByRoleName(name)) {
                roleRepository.save(Role.builder()
                        .roleName(name)
                        .description(name + " role")
                        .build());
            }
        }

        if (!userRepository.existsByEmail(adminEmail)) {
            Integer adminRoleId = roleRepository.findByRoleName("ADMIN")
                    .map(Role::getRoleId).orElse(null);
            userRepository.save(User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .fullName("System Administrator")
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .roleId(adminRoleId)
                    .active(Boolean.TRUE)
                    .status("ACTIVE")
                    .build());
            log.warn("Bootstrap ADMIN account created: {} — change this password immediately.", adminEmail);
        }
    }
}
