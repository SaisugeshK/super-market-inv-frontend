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
public class SupplierOutstandingDto {

    private Long supplierId;
    private String supplierName;
    private BigDecimal totalPurchases;
    private BigDecimal totalPaid;
    private BigDecimal totalPending;
}
