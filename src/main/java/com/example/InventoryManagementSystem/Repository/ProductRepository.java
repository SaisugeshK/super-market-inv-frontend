package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Barcode-based billing: resolve a scanned barcode to a product
    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndProductIdNot(String barcode, Long productId);

    // Bulk import: SKU has a DB-level unique constraint but no app-level check existed
    // for the single-create path (it just relies on DataIntegrityViolationException).
    // Import needs a specific pre-check so it can report a per-row message instead.
    boolean existsBySku(String sku);

    // Checkout: lock the product row so concurrent sales cannot race the stock update
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productId = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

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