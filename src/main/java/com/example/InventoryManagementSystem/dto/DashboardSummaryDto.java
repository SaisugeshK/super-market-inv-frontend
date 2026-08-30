package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {

    // Today's stats
    private BigDecimal todayTotalSales;
    private long todayBillCount;

    // Purchase stats
    private BigDecimal totalPurchaseAmount;
    private BigDecimal totalSupplierPending;

    // Stock stats
    private long lowStockProductCount;
    private List<LowStockItem> lowStockItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockItem {
        private Long productId;
        private String productName;
        private Integer stockQuantity;
        private Integer minimumStock;
    }
}
