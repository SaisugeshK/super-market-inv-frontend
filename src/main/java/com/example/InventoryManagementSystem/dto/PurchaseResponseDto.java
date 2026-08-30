package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponseDto {

    private Long purchaseId;

    private String supplierName;

    private String invoiceNumber;

    private LocalDateTime purchaseDate;

    private BigDecimal totalAmount;

    private BigDecimal tax;

    private BigDecimal paidAmount;

    private BigDecimal pendingAmount;

    private String paymentStatus;

    private String createdBy;

    private Long supplierId;

    private BigDecimal returnedAmount;
}