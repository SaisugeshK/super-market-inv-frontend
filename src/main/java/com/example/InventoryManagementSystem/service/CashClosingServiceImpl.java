package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.BillingCounterRepository;
import com.example.InventoryManagementSystem.Repository.CashClosingRepository;
import com.example.InventoryManagementSystem.Repository.ExpenseRepository;
import com.example.InventoryManagementSystem.Repository.SalesRepository;
import com.example.InventoryManagementSystem.Repository.SalesReturnRepository;
import com.example.InventoryManagementSystem.dto.CashClosingCreateRequestDto;
import com.example.InventoryManagementSystem.dto.CashClosingRequestDto;
import com.example.InventoryManagementSystem.dto.CashClosingResponseDto;
import com.example.InventoryManagementSystem.dto.CashClosingSummaryDto;
import com.example.InventoryManagementSystem.dto.PaymentMethodSummaryDto;
import com.example.InventoryManagementSystem.model.BillingCounter;
import com.example.InventoryManagementSystem.model.CashClosing;
import com.example.InventoryManagementSystem.model.CashClosingPaymentDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class CashClosingServiceImpl implements CashClosingService {

    private static final String CASH = "CASH";

    private final CashClosingRepository cashClosingRepository;
    private final BillingCounterRepository billingCounterRepository;
    private final SalesRepository salesRepository;
    private final SalesReturnRepository salesReturnRepository;
    private final ExpenseRepository expenseRepository;

    // if |difference| for any payment method exceeds this, the closing needs manager approval
    @Value("${cash.closing.difference-threshold:100}")
    private BigDecimal differenceThreshold;

    // ─── SUMMARY (preview, nothing persisted) ────────────────────────────────────
    @Override
    public CashClosingSummaryDto getCounterSummary(Long counterId) {

        BillingCounter counter = billingCounterRepository.findById(counterId)
                .orElseThrow(() -> new RuntimeException("Counter not found"));

        Optional<CashClosing> previous =
                cashClosingRepository.findTopByBillingCounter_CounterIdOrderByCreatedAtDesc(counterId);

        Window window = resolveWindow(previous);

        Map<String, PaymentMethodSummaryDto> calc = computeCalc(counterId, counter, previous, window, null, null);

        BigDecimal totalSales = calc.values().stream()
                .map(PaymentMethodSummaryDto::getSalesAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CashClosingSummaryDto.builder()
                .counter(CashClosingSummaryDto.CounterInfo.builder()
                        .id(counter.getCounterId())
                        .name(counter.getCounterName())
                        .build())
                .session(CashClosingSummaryDto.SessionInfo.builder()
                        .openingDate(window.fromOffset != null ? window.fromOffset.toLocalDate() : null)
                        .lastClosingDate(previous.map(c -> c.getCreatedAt().toLocalDate()).orElse(null))
                        .build())
                .paymentSummary(List.copyOf(calc.values()))
                .totalSales(totalSales)
                .build();
    }

    // ─── CREATE (persists the full calculated snapshot) ──────────────────────────
    @Override
    @Transactional
    public CashClosingResponseDto createCashClosing(CashClosingCreateRequestDto dto) {

        if (dto.getCounterId() == null) {
            throw new RuntimeException("counterId is required");
        }

        BillingCounter counter = billingCounterRepository.findById(dto.getCounterId())
                .orElseThrow(() -> new RuntimeException("Counter not found"));

        Optional<CashClosing> previous = cashClosingRepository
                .findTopByBillingCounter_CounterIdOrderByCreatedAtDesc(dto.getCounterId());

        // rule: an unresolved (PENDING_APPROVAL) closing must be resolved before a new one can be made
        if (previous.isPresent() && "PENDING_APPROVAL".equals(previous.get().getStatus())) {
            throw new RuntimeException(
                    "Previous cash closing for this counter is pending manager approval");
        }

        Map<String, BigDecimal> actualInputs = new LinkedHashMap<>();
        Map<String, BigDecimal> openingOverrides = new LinkedHashMap<>();
        if (dto.getPaymentClosings() != null) {
            for (CashClosingCreateRequestDto.PaymentClosingInput in : dto.getPaymentClosings()) {
                if (in.getPaymentMethod() == null) continue;
                String method = in.getPaymentMethod().toUpperCase();
                actualInputs.put(method, in.getActualAmount());
                // cashier-corrected opening balance (till was reset / miscounted last time) —
                // optional, defaults to the backend-calculated suggestion when omitted
                if (in.getOpeningAmount() != null) {
                    openingOverrides.put(method, in.getOpeningAmount());
                }
            }
        }

        // actual CASH amount is mandatory
        BigDecimal actualCash = actualInputs.get(CASH);
        if (actualCash == null) {
            throw new RuntimeException("Actual CASH amount is required");
        }

        Window window = resolveWindow(previous);
        Map<String, PaymentMethodSummaryDto> calc = computeCalc(
                dto.getCounterId(), counter, previous, window, actualInputs, openingOverrides);

        boolean needsApproval = calc.values().stream()
                .map(PaymentMethodSummaryDto::getDifferenceAmount)
                .anyMatch(diff -> diff != null && diff.abs().compareTo(differenceThreshold) > 0);

        CashClosing closing = CashClosing.builder()
                .billingCounter(counter)
                .openingTime(window.fromOffset)
                .status(needsApproval ? "PENDING_APPROVAL" : "CLOSED")
                .build();

        BigDecimal totalSales = BigDecimal.ZERO;
        for (PaymentMethodSummaryDto row : calc.values()) {
            CashClosingPaymentDetail detail = CashClosingPaymentDetail.builder()
                    .cashClosing(closing)
                    .paymentMethod(row.getPaymentMethod())
                    .openingAmount(row.getOpeningBalance())
                    .salesAmount(row.getSalesAmount())
                    .refundAmount(row.getRefundAmount())
                    .expenseAmount(row.getExpenseAmount())
                    .expectedAmount(row.getExpectedClosing())
                    .actualAmount(row.getActualAmount())
                    .differenceAmount(row.getDifferenceAmount())
                    .build();
            closing.getPaymentDetails().add(detail);
            totalSales = totalSales.add(row.getSalesAmount());
        }

        // back-compat flat fields mirror the CASH row
        PaymentMethodSummaryDto cashRow = calc.get(CASH);
        closing.setOpeningCash(cashRow.getOpeningBalance());
        closing.setClosingCash(cashRow.getActualAmount());
        closing.setTotalSales(totalSales);

        CashClosing saved = cashClosingRepository.save(closing);

        return mapToDto(saved);
    }

    // ─── READ / DELETE / legacy UPDATE ────────────────────────────────────────────
    @Override
    public List<CashClosingResponseDto> getAllCashClosings() {
        return cashClosingRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Override
    public CashClosingResponseDto getCashClosingById(Long id) {
        CashClosing closing = cashClosingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cash closing not found"));
        return mapToDto(closing);
    }

    // Simple field edit for the old flat shape — does not recompute the payment-method snapshot.
    @Override
    public CashClosingResponseDto updateCashClosing(Long id, CashClosingRequestDto dto) {

        CashClosing closing = cashClosingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cash closing not found"));

        BillingCounter counter = billingCounterRepository.findById(dto.getCounterId())
                .orElseThrow(() -> new RuntimeException("Counter not found"));

        closing.setBillingCounter(counter);
        closing.setOpeningCash(dto.getOpeningCash());
        closing.setClosingCash(dto.getClosingCash());
        closing.setTotalSales(dto.getTotalSales());

        return mapToDto(cashClosingRepository.save(closing));
    }

    @Override
    public void deleteCashClosing(Long id) {
        CashClosing closing = cashClosingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cash closing not found"));
        cashClosingRepository.delete(closing);
    }

    // ─── CALCULATION CORE ─────────────────────────────────────────────────────────
    // window covered by "the current open session": everything since the previous
    // closing (exclusive) up to now (inclusive); everything ever, if there is no previous closing.
    private record Window(LocalDateTime fromLocal, LocalDateTime toLocal,
                          OffsetDateTime fromOffset, OffsetDateTime toOffset) {
    }

    private Window resolveWindow(Optional<CashClosing> previous) {
        OffsetDateTime toOffset = OffsetDateTime.now();
        LocalDateTime toLocal = LocalDateTime.now();
        OffsetDateTime fromOffset = previous.map(CashClosing::getCreatedAt).orElse(null);
        LocalDateTime fromLocal = fromOffset != null ? fromOffset.toLocalDateTime() : null;
        return new Window(fromLocal, toLocal, fromOffset, toOffset);
    }

    private Map<String, BigDecimal> toMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String method = String.valueOf(row[0]).toUpperCase();
            map.put(method, toBigDecimal(row[1]));
        }
        return map;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        return new BigDecimal(value.toString());
    }

    // Builds one PaymentMethodSummaryDto per payment method that has appeared in sales,
    // refunds, expenses, or the previous closing (CASH is always included).
    // actualInputs == null      -> preview mode (summary endpoint): actualAmount/differenceAmount left null,
    //                              openingBalance is the backend-calculated suggestion for the frontend to pre-fill.
    // actualInputs != null      -> create mode: overlays the counted amounts and computes the difference.
    // openingOverrides          -> optional, only meaningful in create mode: cashier-corrected opening
    //                              balance per method: used (and persisted) instead of the auto-calculated one.
    private Map<String, PaymentMethodSummaryDto> computeCalc(
            Long counterId,
            BillingCounter counter,
            Optional<CashClosing> previous,
            Window window,
            Map<String, BigDecimal> actualInputs,
            Map<String, BigDecimal> openingOverrides) {

        Map<String, BigDecimal> sales = toMap(window.fromLocal != null
                ? salesRepository.sumSalesByPaymentMethodSince(counterId, window.fromLocal, window.toLocal)
                : salesRepository.sumSalesByPaymentMethodAll(counterId, window.toLocal));

        Map<String, BigDecimal> refunds = toMap(window.fromOffset != null
                ? salesReturnRepository.sumRefundsByPaymentMethodSince(counterId, window.fromOffset, window.toOffset)
                : salesReturnRepository.sumRefundsByPaymentMethodAll(counterId, window.toOffset));

        Map<String, BigDecimal> expenses = toMap(window.fromOffset != null
                ? expenseRepository.sumExpensesByPaymentMethodSince(counterId, window.fromOffset, window.toOffset)
                : expenseRepository.sumExpensesByPaymentMethodAll(counterId, window.toOffset));

        Map<String, BigDecimal> previousActual = new LinkedHashMap<>();
        previous.ifPresent(p -> p.getPaymentDetails().forEach(
                d -> previousActual.put(d.getPaymentMethod(), d.getActualAmount())));

        TreeSet<String> methods = new TreeSet<>();
        methods.add(CASH);
        methods.addAll(sales.keySet());
        methods.addAll(refunds.keySet());
        methods.addAll(expenses.keySet());
        methods.addAll(previousActual.keySet());
        if (actualInputs != null) {
            methods.addAll(actualInputs.keySet());
        }

        Map<String, PaymentMethodSummaryDto> result = new LinkedHashMap<>();
        for (String method : methods) {

            BigDecimal autoOpening = CASH.equals(method) && !previousActual.containsKey(CASH)
                    ? (counter.getInitialOpeningCash() != null ? counter.getInitialOpeningCash() : BigDecimal.ZERO)
                    : previousActual.getOrDefault(method, BigDecimal.ZERO);

            BigDecimal opening = (openingOverrides != null && openingOverrides.containsKey(method))
                    ? openingOverrides.get(method)
                    : autoOpening;

            BigDecimal salesAmt = sales.getOrDefault(method, BigDecimal.ZERO);
            BigDecimal refundAmt = refunds.getOrDefault(method, BigDecimal.ZERO);
            BigDecimal expenseAmt = expenses.getOrDefault(method, BigDecimal.ZERO);
            BigDecimal expected = opening.add(salesAmt).subtract(refundAmt).subtract(expenseAmt);

            BigDecimal actual = actualInputs != null ? actualInputs.get(method) : null;
            BigDecimal difference = actual != null ? actual.subtract(expected) : null;

            result.put(method, PaymentMethodSummaryDto.builder()
                    .paymentMethod(method)
                    .openingBalance(opening)
                    .salesAmount(salesAmt)
                    .refundAmount(refundAmt)
                    .expenseAmount(expenseAmt)
                    .expectedClosing(expected)
                    .actualAmount(actual)
                    .differenceAmount(difference)
                    .build());
        }
        return result;
    }

    private CashClosingResponseDto mapToDto(CashClosing c) {

        List<PaymentMethodSummaryDto> details = c.getPaymentDetails().stream()
                .map(d -> PaymentMethodSummaryDto.builder()
                        .paymentMethod(d.getPaymentMethod())
                        .openingBalance(d.getOpeningAmount())
                        .salesAmount(d.getSalesAmount())
                        .refundAmount(d.getRefundAmount())
                        .expenseAmount(d.getExpenseAmount())
                        .expectedClosing(d.getExpectedAmount())
                        .actualAmount(d.getActualAmount())
                        .differenceAmount(d.getDifferenceAmount())
                        .build())
                .toList();

        BigDecimal differenceCash = details.stream()
                .filter(d -> CASH.equals(d.getPaymentMethod()))
                .map(PaymentMethodSummaryDto::getDifferenceAmount)
                .findFirst()
                .orElse(null);

        return CashClosingResponseDto.builder()
                .closingId(c.getClosingId())
                .counterId(c.getBillingCounter() != null ? c.getBillingCounter().getCounterId() : null)
                .counterName(c.getBillingCounter() != null ? c.getBillingCounter().getCounterName() : null)
                .openingCash(c.getOpeningCash())
                .closingCash(c.getClosingCash())
                .totalSales(c.getTotalSales())
                .createdAt(c.getCreatedAt())
                .status(c.getStatus())
                .differenceCash(differenceCash)
                .paymentDetails(details)
                .build();
    }
}
