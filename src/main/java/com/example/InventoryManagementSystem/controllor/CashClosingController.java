package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.CashClosingCreateRequestDto;
import com.example.InventoryManagementSystem.dto.CashClosingRequestDto;
import com.example.InventoryManagementSystem.dto.CashClosingResponseDto;
import com.example.InventoryManagementSystem.dto.CashClosingSummaryDto;
import com.example.InventoryManagementSystem.service.CashClosingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cash-closing")
@RequiredArgsConstructor
public class CashClosingController {

    private final CashClosingService cashClosingService;

    // Backend-calculated preview for the counter's current open session (opening balance,
    // sales/refunds/expenses per payment method, expected closing). Nothing is persisted.
    @GetMapping("/counter/{counterId}/summary")
    public ResponseEntity<CashClosingSummaryDto> getCounterSummary(
            @PathVariable Long counterId) {

        return ResponseEntity.ok(
                cashClosingService.getCounterSummary(counterId));
    }

    // CREATE — frontend sends only the actual counted amount per payment method;
    // backend recomputes and stores the full snapshot.
    @PostMapping
    public ResponseEntity<CashClosingResponseDto>
    createCashClosing(
            @RequestBody CashClosingCreateRequestDto dto) {

        return ResponseEntity.ok(
                cashClosingService
                        .createCashClosing(dto));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<CashClosingResponseDto>>
    getAllCashClosings() {

        return ResponseEntity.ok(
                cashClosingService
                        .getAllCashClosings());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<CashClosingResponseDto>
    getCashClosingById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                cashClosingService
                        .getCashClosingById(id));
    }

    // UPDATE (legacy flat-field edit only — does not recompute the payment-method snapshot)
    @PutMapping("/{id}")
    public ResponseEntity<CashClosingResponseDto>
    updateCashClosing(
            @PathVariable Long id,
            @RequestBody CashClosingRequestDto dto) {

        return ResponseEntity.ok(
                cashClosingService
                        .updateCashClosing(id, dto));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deleteCashClosing(
            @PathVariable Long id) {

        cashClosingService
                .deleteCashClosing(id);

        return ResponseEntity.ok(
                "Cash closing deleted successfully");
    }
}
