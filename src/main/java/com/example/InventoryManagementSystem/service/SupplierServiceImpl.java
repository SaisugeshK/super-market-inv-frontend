package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.SupplierRequest;
import com.example.InventoryManagementSystem.dto.SupplierResponse;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Supplier;
import com.example.InventoryManagementSystem.Repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    public SupplierResponse createSupplier(SupplierRequest request) {

        if (request.getEmail() != null &&
                supplierRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Supplier email already exists");
        }

        Supplier supplier = Supplier.builder()
                .supplierName(request.getSupplierName())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .gstNumber(request.getGstNumber())
                .status(request.getStatus() != null ? request.getStatus() : "active")
                .build();

        Supplier savedSupplier = supplierRepository.save(supplier);

        return mapToResponse(savedSupplier);
    }

    @Override
    public SupplierResponse getSupplierById(Long supplierId) {

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id : " + supplierId));

        return mapToResponse(supplier);
    }

    @Override
    public List<SupplierResponse> getAllSuppliers() {

        return supplierRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SupplierResponse updateSupplier(Long supplierId, SupplierRequest request) {

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id : " + supplierId));

        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setGstNumber(request.getGstNumber());
        supplier.setStatus(request.getStatus());

        Supplier updatedSupplier = supplierRepository.save(supplier);

        return mapToResponse(updatedSupplier);
    }

    @Override
    public void deleteSupplier(Long supplierId) {

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id : " + supplierId));

        supplierRepository.delete(supplier);
    }

    private SupplierResponse mapToResponse(Supplier supplier) {

        return SupplierResponse.builder()
                .supplierId(supplier.getSupplierId())
                .supplierName(supplier.getSupplierName())
                .contactPerson(supplier.getContactPerson())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .gstNumber(supplier.getGstNumber())
                .status(supplier.getStatus())
                .createdAt(supplier.getCreatedAt())
                .build();
    }
}