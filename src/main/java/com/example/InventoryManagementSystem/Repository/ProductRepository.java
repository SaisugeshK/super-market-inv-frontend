package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Barcode-based billing: resolve a scanned barcode to a product
    Optional<Product> findByBarcode(String barcode);

    // Fast billing: type-to-search by product name or barcode
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.productName) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Product> searchByNameOrBarcode(@Param("term") String term);

    // Low stock: products where stockQuantity <= minimumStock
    @Query("SELECT p FROM Product p WHERE p.stockQuantity IS NOT NULL AND p.minimumStock IS NOT NULL AND p.stockQuantity <= p.minimumStock AND (p.status IS NULL OR LOWER(p.status) <> 'inactive')")
    List<Product> findLowStockProducts();

    // Total pending from supplier across purchases
    @Query("SELECT SUM(pu.pendingAmount) FROM Purchase pu")
    java.math.BigDecimal getTotalSupplierPending();
}