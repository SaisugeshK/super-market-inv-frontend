package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    // Supplier purchase history — use JPQL to avoid derived-query confusion
    @Query("SELECT p FROM Purchase p WHERE p.supplier.supplierId = :supplierId")
    List<Purchase> findBySupplierSupplierId(@Param("supplierId") Long supplierId);

    // Supplier outstanding: total purchases, total paid, total pending
    @Query("SELECT p.supplier.supplierId, p.supplier.supplierName, " +
           "SUM(p.totalAmount), SUM(p.paidAmount), SUM(p.pendingAmount) " +
           "FROM Purchase p GROUP BY p.supplier.supplierId, p.supplier.supplierName")
    List<Object[]> getSupplierOutstandingSummary();
}