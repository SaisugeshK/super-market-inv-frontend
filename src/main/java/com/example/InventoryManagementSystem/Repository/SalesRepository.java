package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesRepository extends JpaRepository<Sales, Long> {

    // Sales between date range
    List<Sales> findBySaleDateBetween(LocalDateTime from, LocalDateTime to);

    // Sales by counter
    List<Sales> findByCounterId(Long counterId);

    boolean existsByCounterId(Long counterId);

    // Today's total sales amount
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sales s WHERE s.saleDate >= :startOfDay")
    BigDecimal getTodayTotalSales(@Param("startOfDay") LocalDateTime startOfDay);

    // Today's bill count
    @Query("SELECT COUNT(s) FROM Sales s WHERE s.saleDate >= :startOfDay")
    long getTodayBillCount(@Param("startOfDay") LocalDateTime startOfDay);

    // Sales total per payment method for a counter within (from, to] — used by cash closing.
    // Split into two queries (rather than "(:from IS NULL OR ...)") because a NULL bind
    // parameter with no other type hint makes Postgres fail with
    // "could not determine data type of parameter" on a plain JPQL comparison.
    @Query("SELECT s.paymentMethod, COALESCE(SUM(s.totalAmount), 0) FROM Sales s " +
           "WHERE s.counterId = :counterId AND s.saleDate > :from AND s.saleDate <= :to " +
           "GROUP BY s.paymentMethod")
    List<Object[]> sumSalesByPaymentMethodSince(@Param("counterId") Long counterId,
                                                @Param("from") LocalDateTime from,
                                                @Param("to") LocalDateTime to);

    @Query("SELECT s.paymentMethod, COALESCE(SUM(s.totalAmount), 0) FROM Sales s " +
           "WHERE s.counterId = :counterId AND s.saleDate <= :to " +
           "GROUP BY s.paymentMethod")
    List<Object[]> sumSalesByPaymentMethodAll(@Param("counterId") Long counterId,
                                              @Param("to") LocalDateTime to);
}