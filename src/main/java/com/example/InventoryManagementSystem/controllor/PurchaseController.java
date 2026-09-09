package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.PurchaseReceiveRequestDto;
import com.example.InventoryManagementSystem.dto.PurchaseRequestDto;
import com.example.InventoryManagementSystem.dto.PurchaseResponseDto;
import com.example.InventoryManagementSystem.service.PurchaseReceiveService;
import com.example.InventoryManagementSystem.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseReceiveService purchaseReceiveService;

    // CREATE PURCHASE (legacy — header only; kept for backward compatibility)
    @PostMapping
    public ResponseEntity<PurchaseResponseDto> createPurchase(
            @RequestBody PurchaseRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseService.createPurchase(request));
    }

    // ATOMIC GOODS RECEIPT — whole purchase, server-computed totals, stock added in one transaction
    @PostMapping("/receive")
    public ResponseEntity<PurchaseResponseDto> receive(
            @Valid @RequestBody PurchaseReceiveRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseReceiveService.receive(request));
    }

    // GET ALL PURCHASES
    @GetMapping
    public ResponseEntity<List<PurchaseResponseDto>> getAllPurchases() {

        return ResponseEntity.ok(
                purchaseService.getAllPurchases());
    }

    // GET PURCHASE BY ID
    @GetMapping("/{purchaseId}")
    public ResponseEntity<PurchaseResponseDto> getPurchaseById(
            @PathVariable Long purchaseId) {

        return ResponseEntity.ok(
                purchaseService.getPurchaseById(purchaseId));
    }

    // UPDATE PURCHASE
    @PutMapping("/{purchaseId}")
    public ResponseEntity<PurchaseResponseDto> updatePurchase(
            @PathVariable Long purchaseId,
            @RequestBody PurchaseRequestDto request) {

        return ResponseEntity.ok(
                purchaseService.updatePurchase(
                        purchaseId,
                        request));
    }

    // DELETE PURCHASE
    @DeleteMapping("/{purchaseId}")
    public ResponseEntity<String> deletePurchase(
            @PathVariable Long purchaseId) {

        purchaseService.deletePurchase(purchaseId);

        return ResponseEntity.ok(
                "Purchase deleted successfully");
    }
}