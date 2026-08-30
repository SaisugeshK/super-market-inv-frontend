package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long purchaseId;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "tax")
    private BigDecimal tax;

    // amount actually paid to supplier
    @Column(name = "paid_amount", precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    // total value of goods sent back to the supplier (sum of purchase-return items)
    @Column(name = "returned_amount", precision = 12, scale = 2)
    private BigDecimal returnedAmount = BigDecimal.ZERO;

    // auto-computed: totalAmount - returnedAmount - paidAmount
    @Column(name = "pending_amount", precision = 12, scale = 2)
    private BigDecimal pendingAmount = BigDecimal.ZERO;

    @Column(name = "payment_status")
    private String paymentStatus; // FULLY_PAID, PARTIALLY_PAID, PENDING

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @PrePersist
    public void setDate() {
        this.purchaseDate = LocalDateTime.now();
        computePending();
    }

    @PreUpdate
    public void preUpdate() {
        computePending();
    }

    private void computePending() {
        BigDecimal total    = this.totalAmount    != null ? this.totalAmount    : BigDecimal.ZERO;
        BigDecimal paid     = this.paidAmount     != null ? this.paidAmount     : BigDecimal.ZERO;
        BigDecimal returned = this.returnedAmount != null ? this.returnedAmount : BigDecimal.ZERO;

        // what you still owe the supplier, net of goods returned
        this.pendingAmount = total.subtract(returned).subtract(paid);

        if (this.pendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            this.paymentStatus = "FULLY_PAID";
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            this.paymentStatus = "PARTIALLY_PAID";
        } else {
            this.paymentStatus = "PENDING";
        }
    }
}