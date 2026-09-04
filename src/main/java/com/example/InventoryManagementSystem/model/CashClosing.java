package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cash_closing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashClosing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "closing_id")
    private Long closingId;

    @ManyToOne
    @JoinColumn(name = "counter_id")
    private BillingCounter billingCounter;

    // kept for backward compatibility with the old single-CASH-only UI/API —
    // mirrors the CASH row in paymentDetails (openingAmount / actualAmount / total across all methods)
    @Column(name = "opening_cash")
    private BigDecimal openingCash;

    @Column(name = "closing_cash")
    private BigDecimal closingCash;

    @Column(name = "total_sales")
    private BigDecimal totalSales;

    // start of the session this closing covers (= previous closing's time, or null for "since the beginning")
    @Column(name = "opening_time")
    private OffsetDateTime openingTime;

    // CLOSED or PENDING_APPROVAL (difference exceeded the configured threshold)
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "cashClosing", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CashClosingPaymentDetail> paymentDetails = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
        if (this.status == null) {
            this.status = "CLOSED";
        }
    }
}
