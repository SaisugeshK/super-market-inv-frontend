package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// GET /api/cash-closing/counter/{counterId}/summary response
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashClosingSummaryDto {

    private CounterInfo counter;
    private SessionInfo session;
    private List<PaymentMethodSummaryDto> paymentSummary;
    private BigDecimal totalSales;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CounterInfo {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionInfo {
        private LocalDate openingDate;
        private LocalDate lastClosingDate;
    }
}
