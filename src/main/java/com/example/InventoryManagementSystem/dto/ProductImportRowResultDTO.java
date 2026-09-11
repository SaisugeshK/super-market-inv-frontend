package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outcome of importing one spreadsheet row from POST /api/products/import.
 * rowNumber matches what the user sees in Excel/a text editor: header = row 1,
 * so the first data row is row 2.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImportRowResultDTO {

    private int rowNumber;
    private String productName;
    private String status; // "created" | "failed"
    private String message;
    private Long productId; // set only when status = "created"
}
