package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.SalesItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesItemRepository extends JpaRepository<SalesItem, Long> {

    List<SalesItem> findBySaleId(Long saleId);

    boolean existsByProductId(Long productId);

    // Product sales report: qty sold + revenue per product
    @Query("SELECT si.productId, SUM(si.quantity), SUM(si.total) FROM SalesItem si GROUP BY si.productId")
    List<Object[]> getProductSalesSummary();

    // Product sales report filtered by sale date range (join SalesItem -> Sales on saleId).
    // Both bounds must be supplied; the service substitutes sensible defaults when a
    // side is missing. The "(:param IS NULL OR ...)" pattern is avoided on purpose —
    // an untyped NULL bind makes Postgres fail with "could not determine data type".
    @Query("SELECT si.productId, SUM(si.quantity), SUM(si.total) " +
           "FROM SalesItem si, Sales s " +
           "WHERE si.saleId = s.saleId " +
           "AND s.saleDate >= :from AND s.saleDate <= :to " +
           "GROUP BY si.productId")
    List<Object[]> getProductSalesSummaryBetween(@Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to);
}