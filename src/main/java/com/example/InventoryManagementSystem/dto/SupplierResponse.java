package com.example.InventoryManagementSystem.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierResponse {

    private Long supplierId;

    private String supplierName;

    private String contactPerson;

    private String phone;

    private String email;

    private String address;

    private String gstNumber;

    private String status;

    private OffsetDateTime createdAt;
}