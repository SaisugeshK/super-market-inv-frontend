package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HoldInvoiceRequestDto {

    @NotBlank(message = "data is required")
    @Size(max = 20000, message = "held-bill payload is too large")
    private String data;
}
