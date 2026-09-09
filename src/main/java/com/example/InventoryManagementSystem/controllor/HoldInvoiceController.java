package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.HoldInvoiceRequestDto;
import com.example.InventoryManagementSystem.dto.HoldInvoiceResponseDto;
import com.example.InventoryManagementSystem.service.HoldInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hold-invoices")
@RequiredArgsConstructor
public class HoldInvoiceController {

    private final HoldInvoiceService holdInvoiceService;

    // CREATE
    @PostMapping
    public ResponseEntity<HoldInvoiceResponseDto>
    createHoldInvoice(
            @Valid @RequestBody HoldInvoiceRequestDto dto) {

        return ResponseEntity.ok(
                holdInvoiceService
                        .createHoldInvoice(dto));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<HoldInvoiceResponseDto>>
    getAllHoldInvoices() {

        return ResponseEntity.ok(
                holdInvoiceService
                        .getAllHoldInvoices());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<HoldInvoiceResponseDto>
    getHoldInvoiceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                holdInvoiceService
                        .getHoldInvoiceById(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<HoldInvoiceResponseDto>
    updateHoldInvoice(
            @PathVariable Long id,
            @Valid @RequestBody HoldInvoiceRequestDto dto) {

        return ResponseEntity.ok(
                holdInvoiceService
                        .updateHoldInvoice(id, dto));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deleteHoldInvoice(
            @PathVariable Long id) {

        holdInvoiceService
                .deleteHoldInvoice(id);

        return ResponseEntity.ok(
                "Hold invoice deleted successfully");
    }
}