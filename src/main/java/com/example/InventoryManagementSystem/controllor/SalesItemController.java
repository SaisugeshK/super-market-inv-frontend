package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.SalesItemRequestDTO;
import com.example.InventoryManagementSystem.dto.SalesItemResponseDTO;
import com.example.InventoryManagementSystem.service.SalesItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-items")
@RequiredArgsConstructor
public class SalesItemController {

    private final SalesItemService salesItemService;

    // CREATE
    @PostMapping
    public ResponseEntity<SalesItemResponseDTO> create(
            @Valid @RequestBody SalesItemRequestDTO dto) {

        return ResponseEntity.ok(
                salesItemService.createSalesItem(dto));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<SalesItemResponseDTO>> getAll() {

        return ResponseEntity.ok(
                salesItemService.getAllSalesItems());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<SalesItemResponseDTO> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                salesItemService.getSalesItemById(id));
    }

    // GET BY SALE ID
    @GetMapping("/sale/{saleId}")
    public ResponseEntity<List<SalesItemResponseDTO>> getBySaleId(
            @PathVariable Long saleId) {

        return ResponseEntity.ok(
                salesItemService.getItemsBySaleId(saleId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalesItemResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody SalesItemRequestDTO dto) {

        return ResponseEntity.ok(
                salesItemService.updateSalesItem(id, dto)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id) {

        salesItemService.deleteSalesItem(id);

        return ResponseEntity.ok(
                "Sales item deleted and stock restored");
    }
}