package com.example.InventoryManagementSystem.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CheckoutResponseDto {

    private Long saleId;
    private String invoiceNumber;

    private Long customerId;
    private Long counterId;
    private String paymentMethod;
    private String paymentStatus;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;

    private LocalDateTime saleDate;

    private List<Line> items;

    @Data
    @Builder
    public static class Line {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private Double taxPercentage;
        private BigDecimal lineSubtotal;
        private BigDecimal lineTax;
        private BigDecimal lineTotal;
    }
}
