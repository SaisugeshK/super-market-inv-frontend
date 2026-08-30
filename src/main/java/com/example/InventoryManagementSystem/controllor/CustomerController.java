package com.example.InventoryManagementSystem.controllor;


import com.example.InventoryManagementSystem.dto.CustomerRequestDTO;
import com.example.InventoryManagementSystem.dto.CustomerResponseDTO;
import com.example.InventoryManagementSystem.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;
    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(@RequestBody CustomerRequestDTO dto) {
        return ResponseEntity.ok(service.createCustomer(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCustomerById(id));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllCustomers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> update(
            @PathVariable Long id,
            @RequestBody CustomerRequestDTO dto) {
        return ResponseEntity.ok(service.updateCustomer(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteCustomer(id);
        return ResponseEntity.ok("Customer deleted successfully");
    }
}