package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.SalesReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface SalesReturnRepository
        extends JpaRepository<SalesReturn, Long> {

    @Query("SELECT COALESCE(SUM(sr.returnQuantity), 0) FROM SalesReturn sr " +
           "WHERE sr.salesItemId = :salesItemId")
    int sumReturnedQtyForItem(@Param("salesItemId") Long salesItemId);

    // refund total per payment method for a counter within (from, to] — used by cash closing.
    // Joins to Sales (via saleId) to get the counter and the original payment method;
    // rejected refunds don't reduce cash so they are excluded.
    // Split into two queries rather than "(:from IS NULL OR ...)" — a NULL bind parameter
    // with no other type hint makes Postgres fail with "could not determine data type".
    @Query("SELECT s.paymentMethod, COALESCE(SUM(sr.totalAmount), 0) " +
           "FROM SalesReturn sr, Sales s " +
           "WHERE sr.saleId = s.saleId AND s.counterId = :counterId " +
           "AND (sr.refundStatus IS NULL OR sr.refundStatus <> 'REJECTED') " +
           "AND sr.returnDate > :from AND sr.returnDate <= :to " +
           "GROUP BY s.paymentMethod")
    List<Object[]> sumRefundsByPaymentMethodSince(@Param("counterId") Long counterId,
                                                  @Param("from") OffsetDateTime from,
                                                  @Param("to") OffsetDateTime to);

    @Query("SELECT s.paymentMethod, COALESCE(SUM(sr.totalAmount), 0) " +
           "FROM SalesReturn sr, Sales s " +
           "WHERE sr.saleId = s.saleId AND s.counterId = :counterId " +
           "AND (sr.refundStatus IS NULL OR sr.refundStatus <> 'REJECTED') " +
           "AND sr.returnDate <= :to " +
           "GROUP BY s.paymentMethod")
    List<Object[]> sumRefundsByPaymentMethodAll(@Param("counterId") Long counterId,
                                                @Param("to") OffsetDateTime to);
}