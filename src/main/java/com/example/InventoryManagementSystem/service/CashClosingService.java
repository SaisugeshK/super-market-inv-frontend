package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.CashClosingCreateRequestDto;
import com.example.InventoryManagementSystem.dto.CashClosingRequestDto;
import com.example.InventoryManagementSystem.dto.CashClosingResponseDto;
import com.example.InventoryManagementSystem.dto.CashClosingSummaryDto;

import java.util.List;

public interface CashClosingService {

    // backend-calculated preview for the counter's current (still open) session
    CashClosingSummaryDto getCounterSummary(Long counterId);

    // frontend sends only the actual counted amounts; backend recomputes + stores everything else
    CashClosingResponseDto createCashClosing(CashClosingCreateRequestDto dto);

    List<CashClosingResponseDto> getAllCashClosings();

    CashClosingResponseDto getCashClosingById(Long id);

    // simple field edit — does not recompute the payment-method snapshot
    CashClosingResponseDto updateCashClosing(Long id, CashClosingRequestDto dto);

    void deleteCashClosing(Long id);
}
