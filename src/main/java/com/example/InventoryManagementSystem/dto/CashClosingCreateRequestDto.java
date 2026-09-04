package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// POST /api/cash-closing request. Frontend sends the actual counted/reconciled amount
// per payment method — backend independently recomputes sales/refund/expense/expected
// from transaction records and stores the full snapshot.
//
// openingAmount is optional: the summary endpoint pre-fills it as a suggestion
// (previous closing's actual, or the counter's initial opening cash), but the cashier
// can correct it (till was reset, miscounted last time, etc). Omit/null to accept the
// backend-calculated suggestion as-is.
@Data
public class CashClosingCreateRequestDto {

    private Long counterId;
    private List<PaymentClosingInput> paymentClosings;

    @Data
    public static class PaymentClosingInput {
        private String paymentMethod;
        private BigDecimal actualAmount;
        private BigDecimal openingAmount;
    }
}
