package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UnitRepository
        extends JpaRepository<Unit, Long> {

    // Bulk import: a spreadsheet "Unit" column may hold either the full name
    // ("Kilogram") or the short code ("KG") already used elsewhere in the app —
    // match either, case-insensitively.
    @Query("SELECT u FROM Unit u WHERE LOWER(u.unitName) = LOWER(:name) OR LOWER(u.shortName) = LOWER(:name)")
    Optional<Unit> findByNameIgnoreCase(@Param("name") String name);
}
