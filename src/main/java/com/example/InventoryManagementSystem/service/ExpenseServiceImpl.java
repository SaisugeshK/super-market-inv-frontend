package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.ExpenseRepository;
import com.example.InventoryManagementSystem.dto.ExpenseRequestDTO;
import com.example.InventoryManagementSystem.dto.ExpenseResponseDTO;
import com.example.InventoryManagementSystem.model.Expense;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository repository;

    @Override
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto) {

        if (dto.getCounterId() == null) {
            throw new RuntimeException("counterId is required");
        }
        if (dto.getPaymentMethod() == null || dto.getPaymentMethod().isBlank()) {
            throw new RuntimeException("paymentMethod is required");
        }
        if (dto.getAmount() == null) {
            throw new RuntimeException("amount is required");
        }

        Expense expense = Expense.builder()
                .counterId(dto.getCounterId())
                .paymentMethod(dto.getPaymentMethod().toUpperCase())
                .amount(dto.getAmount())
                .note(dto.getNote())
                .build();

        return map(repository.save(expense));
    }

    @Override
    public List<ExpenseResponseDTO> getAllExpenses(Long counterId) {

        List<Expense> expenses = counterId != null
                ? repository.findByCounterId(counterId)
                : repository.findAll();

        return expenses.stream().map(this::map).collect(Collectors.toList());
    }

    @Override
    public ExpenseResponseDTO getExpenseById(Long id) {
        return repository.findById(id)
                .map(this::map)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    @Override
    public void deleteExpense(Long id) {
        Expense expense = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        repository.delete(expense);
    }

    private ExpenseResponseDTO map(Expense e) {
        return ExpenseResponseDTO.builder()
                .expenseId(e.getExpenseId())
                .counterId(e.getCounterId())
                .paymentMethod(e.getPaymentMethod())
                .amount(e.getAmount())
                .note(e.getNote())
                .expenseDate(e.getExpenseDate())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
