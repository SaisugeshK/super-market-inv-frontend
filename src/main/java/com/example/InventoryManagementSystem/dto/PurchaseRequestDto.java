package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseRequestDto {

    private Long supplierId;

    private String invoiceNumber;

    private BigDecimal totalAmount;

    private BigDecimal tax;

    // amount paid upfront (0 = fully pending)
    private BigDecimal paidAmount;

    // FULLY_PAID / PARTIALLY_PAID / PENDING — auto-derived if not set
    private String paymentStatus;

    private Long createdBy;
}