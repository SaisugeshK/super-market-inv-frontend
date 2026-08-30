package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    boolean existsByCounterId(Integer counterId);
}