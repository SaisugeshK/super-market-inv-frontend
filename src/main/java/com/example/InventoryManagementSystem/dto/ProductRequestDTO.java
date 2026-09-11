package com.example.InventoryManagementSystem.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDTO {

    private Long categoryId;

    @NotBlank(message = "productName is required")
    private String productName;
    private String sku;
    private String barcode;

    @PositiveOrZero(message = "purchasePrice cannot be negative")
    private BigDecimal purchasePrice;

    @PositiveOrZero(message = "sellingPrice cannot be negative")
    private BigDecimal sellingPrice;

    @PositiveOrZero(message = "stockQuantity cannot be negative")
    private Integer stockQuantity;

    @PositiveOrZero(message = "minimumStock cannot be negative")
    private Integer minimumStock;
    private String unit;
    private String status;
}