package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByInvoiceId(Long invoiceId);
}
