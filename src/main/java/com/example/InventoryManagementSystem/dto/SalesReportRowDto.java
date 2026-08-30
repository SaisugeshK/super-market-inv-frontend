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
public class SalesReportRowDto {

    private Long saleId;
    private String invoiceNumber;
    private LocalDateTime saleDate;

    private Long customerId;
    private String customerName;

    private Long counterId;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal totalAmount;

    private List<Line> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Line {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal sellingPrice;
        private BigDecimal total;
    }
}
