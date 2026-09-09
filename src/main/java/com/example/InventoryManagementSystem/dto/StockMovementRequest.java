package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Movement type is required")
    private String movementType;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    private Long referenceId;

    private String notes;
}