package com.example.InventoryManagementSystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * A whole goods-received note in one request. The server computes the totals
 * from the line values, adds stock and writes everything in one transaction.
 * Purchase price is supplier-billed data and comes from the client (unlike a
 * sale, where the price is the catalogue price).
 */
@Data
public class PurchaseReceiveRequestDto {

    @NotNull(message = "supplierId is required")
    private Long supplierId;

    private Long createdBy;

    private String invoiceNumber;

    @PositiveOrZero(message = "paidAmount cannot be negative")
    private BigDecimal paidAmount;

    @NotEmpty(message = "at least one line item is required")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "productId is required")
        private Long productId;

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be positive")
        private Integer quantity;

        @NotNull(message = "purchasePrice is required")
        @PositiveOrZero(message = "purchasePrice cannot be negative")
        private BigDecimal purchasePrice;

        @PositiveOrZero(message = "taxAmount cannot be negative")
        private BigDecimal taxAmount;
    }
}
