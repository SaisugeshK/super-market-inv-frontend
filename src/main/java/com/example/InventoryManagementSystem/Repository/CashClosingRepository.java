package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.CashClosing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashClosingRepository
        extends JpaRepository<CashClosing, Long> {

    boolean existsByBillingCounter_CounterId(Long counterId);
}