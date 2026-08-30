package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesRequestDTO {

    private Long customerId;    // optional for walk-in customers
    private Long createdBy;
    private Long counterId;     // billing counter id

    private String invoiceNumber;   // optional — auto-generated in service if blank
    private String paymentMethod;   // CASH, UPI, CARD
    private String paymentStatus;

    private BigDecimal totalAmount;
}