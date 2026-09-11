package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.BillingCounterDto;
import com.example.InventoryManagementSystem.service.BillingCounterService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing-counters")
public class BillingCounterController {

    @Autowired
    private BillingCounterService service;

    // CREATE
    @PostMapping
    public BillingCounterDto createBillingCounter(
            @Valid @RequestBody BillingCounterDto dto) {

        return service.createBillingCounter(dto);
    }

    // READ ALL
    @GetMapping
    public List<BillingCounterDto> getAllBillingCounters() {

        return service.getAllBillingCounters();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public BillingCounterDto getBillingCounterById(
            @PathVariable Long id) {

        return service.getBillingCounterById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public BillingCounterDto updateBillingCounter(
            @PathVariable Long id,
            @Valid @RequestBody BillingCounterDto dto) {

        return service.updateBillingCounter(id, dto);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String deleteBillingCounter(@PathVariable Long id) {

        service.deleteBillingCounter(id);

        return "Billing Counter deleted successfully";
    }
}