package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    private String supplierName;

    private String contactPerson;

    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String address;

    private String gstNumber;

    private String status;
}