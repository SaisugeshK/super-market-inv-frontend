package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.ExpenseRequestDTO;
import com.example.InventoryManagementSystem.dto.ExpenseResponseDTO;
import com.example.InventoryManagementSystem.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> create(@RequestBody ExpenseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createExpense(dto));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDTO>> getAll(
            @RequestParam(required = false) Long counterId) {
        return ResponseEntity.ok(service.getAllExpenses(counterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getExpenseById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteExpense(id);
        return ResponseEntity.ok("Expense deleted successfully");
    }
}
