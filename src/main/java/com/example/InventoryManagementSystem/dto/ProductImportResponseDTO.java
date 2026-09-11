package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImportResponseDTO {

    private int totalRows;
    private int succeeded;
    private int failed;
    private List<ProductImportRowResultDTO> rows;
}
