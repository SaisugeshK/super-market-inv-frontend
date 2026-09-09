package com.example.InventoryManagementSystem.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementResponse {

    private Long movementId;

    private Long productId;

    private String productName;

    private String movementType;

    private Integer quantity;

    private Long referenceId;

    private String notes;

    private OffsetDateTime createdAt;
}