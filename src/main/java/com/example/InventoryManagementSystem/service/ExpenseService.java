package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.ExpenseRequestDTO;
import com.example.InventoryManagementSystem.dto.ExpenseResponseDTO;

import java.util.List;

public interface ExpenseService {

    ExpenseResponseDTO createExpense(ExpenseRequestDTO dto);

    List<ExpenseResponseDTO> getAllExpenses(Long counterId);

    ExpenseResponseDTO getExpenseById(Long id);

    void deleteExpense(Long id);
}
