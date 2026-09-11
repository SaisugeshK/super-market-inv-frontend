package com.example.InventoryManagementSystem.dto;



import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequestDTO {

    @NotBlank(message = "customerName is required")
    private String customerName;
    private String phone;
    private String email;
    private String address;
    private String status;
}