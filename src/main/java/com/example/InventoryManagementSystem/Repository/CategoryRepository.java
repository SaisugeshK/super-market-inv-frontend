package com.example.InventoryManagementSystem.Repository;


import com.example.InventoryManagementSystem.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryName(String categoryName);

    boolean existsByCategoryName(String categoryName);

    // Bulk import: resolve a spreadsheet category name to an existing row
    // case-insensitively, so "snacks" / "Snacks" / "SNACKS" all match one category.
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);
}