package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalesResponseDTO {

    private Long saleId;
    private Long customerId;
    private Long createdBy;
    private Long counterId;         // billing counter
    private String invoiceNumber;
    private String paymentMethod;   // CASH, UPI, CARD
    private String paymentStatus;
    private BigDecimal totalAmount;
    private LocalDateTime saleDate;
}