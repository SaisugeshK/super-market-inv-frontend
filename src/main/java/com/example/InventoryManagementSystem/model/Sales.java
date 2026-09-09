package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Getter
@Setter
public class Sales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saleId;

    private Long customerId;

    private Long createdBy;

    // billing counter that processed this sale
    @Column(name = "counter_id")
    private Long counterId;

    private String invoiceNumber;

    private String paymentMethod; // CASH, UPI, CARD

    private String paymentStatus;

    private BigDecimal totalAmount;

    // Money breakdown — populated by the transactional checkout so the sale total
    // is reconstructable and not dependent on client-supplied numbers.
    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "balance_amount", precision = 12, scale = 2)
    private BigDecimal balanceAmount;

    private LocalDateTime saleDate = LocalDateTime.now();
}