package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    boolean existsByRoleName(String roleName);

    Optional<Role> findByRoleName(String roleName);
}
