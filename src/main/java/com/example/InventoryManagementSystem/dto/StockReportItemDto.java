package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReportItemDto {

    private Long productId;
    private String productName;
    private String barcode;
    private String unit;
    private Integer stockQuantity;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private BigDecimal stockValue; // stockQuantity * purchasePrice
    private boolean lowStock;      // stockQuantity <= minimumStock
}
