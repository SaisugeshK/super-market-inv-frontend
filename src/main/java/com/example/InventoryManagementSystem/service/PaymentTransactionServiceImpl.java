package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.*;
import com.example.InventoryManagementSystem.model.PaymentTransaction;
import com.example.InventoryManagementSystem.model.Sales;
import com.example.InventoryManagementSystem.Repository.PaymentTransactionRepository;
import com.example.InventoryManagementSystem.Repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    private final PaymentTransactionRepository repo;
    private final SalesRepository salesRepository;

    private static BigDecimal money(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public PaymentTransactionResponseDTO create(PaymentTransactionRequestDTO dto) {

        // In the live POS flow the "invoiceId" carried by a payment is the sale id.
        Sales sale = salesRepository.findById(dto.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Sale not found for payment: " + dto.getInvoiceId()));

        BigDecimal grandTotal = money(sale.getTotalAmount());
        BigDecimal alreadyPaid = repo.findByInvoiceId(dto.getInvoiceId()).stream()
                .map(PaymentTransaction::getAmount)
                .filter(a -> a != null)
                .map(PaymentTransactionServiceImpl::money)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal amount = money(dto.getAmount());
        BigDecimal outstanding = grandTotal.subtract(alreadyPaid);

        if (amount.subtract(outstanding).compareTo(new BigDecimal("0.01")) > 0) {
            throw new RuntimeException("Payment " + amount
                    + " exceeds the outstanding balance " + outstanding);
        }

        PaymentTransaction p = PaymentTransaction.builder()
                .invoiceId(dto.getInvoiceId())
                .paymentMethod(dto.getPaymentMethod())
                .transactionReference(dto.getTransactionReference())
                .amount(amount)
                .build();
        repo.save(p);

        BigDecimal paidNow = money(alreadyPaid.add(amount));
        BigDecimal balance = money(grandTotal.subtract(paidNow));
        sale.setPaidAmount(paidNow);
        sale.setBalanceAmount(balance.signum() < 0 ? BigDecimal.ZERO.setScale(2) : balance);

        String status;
        if (paidNow.compareTo(grandTotal) >= 0) {
            status = "PAID";
        } else if (paidNow.signum() > 0) {
            status = "PARTIAL";
        } else {
            status = "PENDING";
        }
        sale.setPaymentStatus(status);
        salesRepository.save(sale);

        return map(p);
    }

    @Override
    public List<PaymentTransactionResponseDTO> getAll() {
        return repo.findAll().stream().map(this::map).toList();
    }

    @Override
    public PaymentTransactionResponseDTO getById(Long id) {
        PaymentTransaction p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return map(p);
    }

    @Override
    public PaymentTransactionResponseDTO update(Long id, PaymentTransactionRequestDTO dto) {

        PaymentTransaction p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        p.setInvoiceId(dto.getInvoiceId());
        p.setPaymentMethod(dto.getPaymentMethod());
        p.setTransactionReference(dto.getTransactionReference());
        p.setAmount(dto.getAmount());

        repo.save(p);

        return map(p);
    }

    @Override
    public void delete(Long id) {
        PaymentTransaction p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        repo.delete(p);
    }

    private PaymentTransactionResponseDTO map(PaymentTransaction p) {
        return PaymentTransactionResponseDTO.builder()
                .transactionId(p.getTransactionId())
                .invoiceId(p.getInvoiceId())
                .paymentMethod(p.getPaymentMethod())
                .transactionReference(p.getTransactionReference())
                .amount(p.getAmount())
                .paymentDate(p.getPaymentDate())
                .build();
    }
}