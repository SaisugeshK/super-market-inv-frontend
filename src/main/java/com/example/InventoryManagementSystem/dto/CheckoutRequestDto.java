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
 * A whole POS sale in one request. The server computes every monetary value
 * from product prices + taxes; only quantities, the discount and the amount
 * tendered come from the client.
 */
@Data
public class CheckoutRequestDto {

    private Long customerId;
    private Long counterId;
    private Long createdBy;

    private String paymentMethod;

    @PositiveOrZero(message = "discountAmount cannot be negative")
    private BigDecimal discountAmount;

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
    }
}
