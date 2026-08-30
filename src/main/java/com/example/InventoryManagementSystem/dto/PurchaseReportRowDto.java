package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReportRowDto {

    private Long purchaseId;
    private String supplierName;
    private String invoiceNumber;
    private LocalDateTime purchaseDate;

    private BigDecimal totalAmount;
    private BigDecimal tax;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private String paymentStatus;
    private String createdBy;

    private List<Line> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Line {
        private Long productId;
        private String productName;
        private Integer quantity;
        private String unit;
        private BigDecimal purchasePrice;
        private BigDecimal taxAmount;
        private BigDecimal total;
    }
}
