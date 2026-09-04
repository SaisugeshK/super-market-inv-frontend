package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashClosingResponseDto {

    private Long closingId;

    private Long counterId;

    private String counterName;

    // CASH-only fields, kept for backward compatibility with the old flat UI —
    // mirror the CASH row in paymentDetails
    private BigDecimal openingCash;

    private BigDecimal closingCash;

    // sum of salesAmount across every payment method
    private BigDecimal totalSales;

    private OffsetDateTime createdAt;

    // CLOSED or PENDING_APPROVAL
    private String status;

    // CASH row's (actualAmount - expectedClosing), for a quick-glance shortage/excess indicator
    private BigDecimal differenceCash;

    // full per-payment-method breakdown (CASH, GPAY, PHONEPE, CARD, ...)
    private List<PaymentMethodSummaryDto> paymentDetails;
}
