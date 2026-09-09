package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.CheckoutRequestDto;
import com.example.InventoryManagementSystem.dto.CheckoutResponseDto;
import com.example.InventoryManagementSystem.dto.SalesRequestDTO;
import com.example.InventoryManagementSystem.dto.SalesResponseDTO;
import com.example.InventoryManagementSystem.service.CheckoutService;
import com.example.InventoryManagementSystem.service.SalesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;
    private final CheckoutService checkoutService;

    // CREATE (legacy — header only; kept for backward compatibility)
    @PostMapping
    public ResponseEntity<SalesResponseDTO> createSale(@RequestBody SalesRequestDTO dto) {
        return ResponseEntity.ok(salesService.createSale(dto));
    }

    // ATOMIC CHECKOUT — whole sale, server-computed money, stock locked
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDto> checkout(@Valid @RequestBody CheckoutRequestDto dto) {
        return ResponseEntity.ok(checkoutService.checkout(dto));
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<SalesResponseDTO> getSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(salesService.getSaleById(id));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<SalesResponseDTO>> getAllSales() {
        return ResponseEntity.ok(salesService.getAllSales());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<SalesResponseDTO> updateSale(
            @PathVariable Long id,
            @RequestBody SalesRequestDTO dto) {
        return ResponseEntity.ok(salesService.updateSale(id, dto));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSale(@PathVariable Long id) {
        salesService.deleteSale(id);
        return ResponseEntity.ok("Sale deleted successfully");
    }
}