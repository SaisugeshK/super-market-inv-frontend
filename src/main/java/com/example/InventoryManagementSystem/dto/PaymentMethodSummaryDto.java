package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// One payment method's row in a cash-closing summary/result: CASH, GPAY, PHONEPE, CARD,
// BANK_TRANSFER, CREDIT, OTHER, ... — every value except actualAmount is backend-calculated
// from transaction records, never trust a frontend-supplied number here.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodSummaryDto {

    private String paymentMethod;
    private BigDecimal openingBalance;
    private BigDecimal salesAmount;
    private BigDecimal refundAmount;
    private BigDecimal expenseAmount;
    private BigDecimal expectedClosing;

    // present only after the counted amount is submitted (create response) — null on the summary preview
    private BigDecimal actualAmount;
    private BigDecimal differenceAmount;
}
