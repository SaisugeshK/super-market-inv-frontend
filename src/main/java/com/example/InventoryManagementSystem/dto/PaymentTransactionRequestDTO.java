package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentTransactionRequestDTO {

    @NotNull(message = "invoiceId is required")
    private Long invoiceId;

    private String paymentMethod;
    private String transactionReference;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;
}
