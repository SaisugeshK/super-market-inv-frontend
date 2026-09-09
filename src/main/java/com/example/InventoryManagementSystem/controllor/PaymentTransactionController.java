package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.*;
import com.example.InventoryManagementSystem.service.PaymentTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentTransactionController {

    private final PaymentTransactionService service;

    // CREATE
    @PostMapping
    public PaymentTransactionResponseDTO create(@Valid @RequestBody PaymentTransactionRequestDTO dto) {
        return service.create(dto);
    }

    // GET ALL
    @GetMapping
    public List<PaymentTransactionResponseDTO> getAll() {
        return service.getAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public PaymentTransactionResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public PaymentTransactionResponseDTO update(
            @PathVariable Long id,
            @RequestBody PaymentTransactionRequestDTO dto) {
        return service.update(id, dto);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Deleted Payment with ID: " + id;
    }

    // 🔥 TEST ENDPOINT (IMPORTANT FOR DEBUG 404)
    @GetMapping("/test")
    public String test() {
        return "PAYMENT MODULE WORKING";
    }
}