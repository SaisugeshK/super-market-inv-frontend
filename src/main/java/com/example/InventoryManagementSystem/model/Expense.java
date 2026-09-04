package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// Cash-drawer / digital-wallet expense recorded against a counter (e.g. paying a delivery
// boy from the till). Feeds the "expenseAmount" deduction in cash-closing reconciliation.
@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long expenseId;

    @Column(name = "counter_id")
    private Long counterId;

    // which drawer/account the expense was paid out of: CASH, GPAY, PHONEPE, CARD, ...
    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    private BigDecimal amount;

    private String note;

    @Column(name = "expense_date")
    private OffsetDateTime expenseDate;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
        if (this.expenseDate == null) {
            this.expenseDate = OffsetDateTime.now();
        }
    }
}
