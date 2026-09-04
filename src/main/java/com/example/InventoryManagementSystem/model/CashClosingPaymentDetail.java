package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// One row per payment method (CASH, GPAY, PHONEPE, CARD, BANK_TRANSFER, CREDIT, OTHER, ...)
// inside a single cash-closing session. Backend-calculated snapshot — never trust
// frontend-computed opening/sales/refund/expense/expected values, only actualAmount.
@Entity
@Table(name = "cash_closing_payment_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashClosingPaymentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cash_closing_id")
    private CashClosing cashClosing;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;

    @Column(name = "opening_amount", precision = 12, scale = 2)
    private BigDecimal openingAmount;

    @Column(name = "sales_amount", precision = 12, scale = 2)
    private BigDecimal salesAmount;

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "expense_amount", precision = 12, scale = 2)
    private BigDecimal expenseAmount;

    @Column(name = "expected_amount", precision = 12, scale = 2)
    private BigDecimal expectedAmount;

    @Column(name = "actual_amount", precision = 12, scale = 2)
    private BigDecimal actualAmount;

    @Column(name = "difference_amount", precision = 12, scale = 2)
    private BigDecimal differenceAmount;
}
