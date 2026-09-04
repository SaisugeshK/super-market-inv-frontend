package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.CashClosing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CashClosingRepository
        extends JpaRepository<CashClosing, Long> {

    boolean existsByBillingCounter_CounterId(Long counterId);

    // most recent closing for a counter — its CASH/other actualAmounts become the next session's opening balances
    Optional<CashClosing> findTopByBillingCounter_CounterIdOrderByCreatedAtDesc(Long counterId);
}