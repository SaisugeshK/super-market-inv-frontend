package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseRequestDTO {

    private Long counterId;
    private String paymentMethod;
    private BigDecimal amount;
    private String note;
}
