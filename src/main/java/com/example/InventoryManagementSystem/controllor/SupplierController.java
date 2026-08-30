package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.SupplierRequest;
import com.example.InventoryManagementSystem.dto.SupplierResponse;
import com.example.InventoryManagementSystem.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody SupplierRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierService.createSupplier(request));
    }

    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> getSupplierById(
            @PathVariable Long supplierId) {

        return ResponseEntity.ok(
                supplierService.getSupplierById(supplierId));
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {

        return ResponseEntity.ok(
                supplierService.getAllSuppliers());
    }

    @PutMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long supplierId,
            @Valid @RequestBody SupplierRequest request) {

        return ResponseEntity.ok(
                supplierService.updateSupplier(supplierId, request));
    }

    @DeleteMapping("/{supplierId}")
    public ResponseEntity<String> deleteSupplier(
            @PathVariable Long supplierId) {

        supplierService.deleteSupplier(supplierId);

        return ResponseEntity.ok("Supplier deleted successfully");
    }
}