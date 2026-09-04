package com.example.InventoryManagementSystem.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class ExpenseResponseDTO {

    private Long expenseId;
    private Long counterId;
    private String paymentMethod;
    private BigDecimal amount;
    private String note;
    private OffsetDateTime expenseDate;
    private OffsetDateTime createdAt;
}
