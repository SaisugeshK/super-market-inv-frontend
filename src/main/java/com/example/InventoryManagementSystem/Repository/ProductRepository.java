package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Low stock: products where stockQuantity <= minimumStock
    @Query("SELECT p FROM Product p WHERE p.stockQuantity IS NOT NULL AND p.minimumStock IS NOT NULL AND p.stockQuantity <= p.minimumStock")
    List<Product> findLowStockProducts();

    // Total pending from supplier across purchases
    @Query("SELECT SUM(pu.pendingAmount) FROM Purchase pu")
    java.math.BigDecimal getTotalSupplierPending();
}