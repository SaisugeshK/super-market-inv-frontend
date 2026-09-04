package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCounterId(Long counterId);

    // expense total per payment method for a counter within (from, to] — used by cash closing.
    // Split into two queries rather than "(:from IS NULL OR ...)" — a NULL bind parameter
    // with no other type hint makes Postgres fail with "could not determine data type".
    @Query("SELECT e.paymentMethod, COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.counterId = :counterId AND e.expenseDate > :from AND e.expenseDate <= :to " +
           "GROUP BY e.paymentMethod")
    List<Object[]> sumExpensesByPaymentMethodSince(@Param("counterId") Long counterId,
                                                   @Param("from") OffsetDateTime from,
                                                   @Param("to") OffsetDateTime to);

    @Query("SELECT e.paymentMethod, COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.counterId = :counterId AND e.expenseDate <= :to " +
           "GROUP BY e.paymentMethod")
    List<Object[]> sumExpensesByPaymentMethodAll(@Param("counterId") Long counterId,
                                                 @Param("to") OffsetDateTime to);
}
