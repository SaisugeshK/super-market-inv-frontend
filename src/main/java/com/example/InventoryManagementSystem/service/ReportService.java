package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseRepository;
import com.example.InventoryManagementSystem.Repository.SalesItemRepository;
import com.example.InventoryManagementSystem.Repository.SalesRepository;
import com.example.InventoryManagementSystem.dto.PurchaseResponseDto;
import com.example.InventoryManagementSystem.dto.SalesResponseDTO;
import com.example.InventoryManagementSystem.dto.StockReportItemDto;
import com.example.InventoryManagementSystem.dto.SupplierOutstandingDto;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.Purchase;
import com.example.InventoryManagementSystem.model.Sales;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SalesRepository salesRepository;
    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final SalesItemRepository salesItemRepository;

    // ─── SALES REPORT ────────────────────────────────────────────────────────────
    public List<SalesResponseDTO> getSalesReport(
            LocalDateTime from,
            LocalDateTime to,
            Long counterId,
            String paymentMethod) {

        List<Sales> sales;

        if (from != null && to != null) {
            sales = salesRepository.findBySaleDateBetween(from, to);
        } else {
            sales = salesRepository.findAll();
        }

        return sales.stream()
                .filter(s -> counterId == null || counterId.equals(s.getCounterId()))
                .filter(s -> paymentMethod == null || paymentMethod.equalsIgnoreCase(s.getPaymentMethod()))
                .map(this::mapSalesToDto)
                .collect(Collectors.toList());
    }

    // ─── PURCHASE REPORT ─────────────────────────────────────────────────────────
    public List<PurchaseResponseDto> getPurchaseReport(
            Long supplierId,
            LocalDateTime from,
            LocalDateTime to) {

        List<Purchase> purchases;

        if (supplierId != null) {
            purchases = purchaseRepository.findBySupplierSupplierId(supplierId);
        } else {
            purchases = purchaseRepository.findAll();
        }

        return purchases.stream()
                .filter(p -> from == null || !p.getPurchaseDate().isBefore(from))
                .filter(p -> to   == null || !p.getPurchaseDate().isAfter(to))
                .map(this::mapPurchaseToDto)
                .collect(Collectors.toList());
    }

    // ─── SUPPLIER OUTSTANDING REPORT ─────────────────────────────────────────────
    public List<SupplierOutstandingDto> getSupplierOutstanding() {

        List<Object[]> rows = purchaseRepository.getSupplierOutstandingSummary();
        List<SupplierOutstandingDto> result = new ArrayList<>();

        for (Object[] row : rows) {
            result.add(SupplierOutstandingDto.builder()
                    .supplierId(((Number) row[0]).longValue())
                    .supplierName((String) row[1])
                    .totalPurchases(toBigDecimal(row[2]))
                    .totalPaid(toBigDecimal(row[3]))
                    .totalPending(toBigDecimal(row[4]))
                    .build());
        }
        return result;
    }

    // ─── STOCK REPORT ────────────────────────────────────────────────────────────
    public List<StockReportItemDto> getStockReport() {

        return productRepository.findAll().stream()
                .map(p -> {
                    int qty = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
                    int minStock = p.getMinimumStock() != null ? p.getMinimumStock() : 0;
                    BigDecimal purchasePrice = p.getPurchasePrice() != null ? p.getPurchasePrice() : BigDecimal.ZERO;
                    BigDecimal stockValue = purchasePrice.multiply(BigDecimal.valueOf(qty));

                    return StockReportItemDto.builder()
                            .productId(p.getProductId())
                            .productName(p.getProductName())
                            .barcode(p.getBarcode())
                            .unit(p.getUnit())
                            .stockQuantity(qty)
                            .purchasePrice(purchasePrice)
                            .sellingPrice(p.getSellingPrice())
                            .stockValue(stockValue)
                            .lowStock(qty <= minStock)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ─── PRODUCT SALES REPORT ────────────────────────────────────────────────────
    public List<java.util.Map<String, Object>> getProductSalesReport() {

        List<Object[]> rows = salesItemRepository.getProductSalesSummary();
        List<java.util.Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rows) {
            Long productId = ((Number) row[0]).longValue();
            String productName = productRepository.findById(productId)
                    .map(Product::getProductName)
                    .orElse("Unknown");

            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("productId", productId);
            item.put("productName", productName);
            item.put("quantitySold", row[1]);
            item.put("totalRevenue", toBigDecimal(row[2]));
            result.add(item);
        }
        return result;
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────────

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        return new BigDecimal(value.toString());
    }

    private SalesResponseDTO mapSalesToDto(Sales s) {
        SalesResponseDTO dto = new SalesResponseDTO();
        dto.setSaleId(s.getSaleId());
        dto.setCustomerId(s.getCustomerId());
        dto.setCreatedBy(s.getCreatedBy());
        dto.setInvoiceNumber(s.getInvoiceNumber());
        dto.setPaymentStatus(s.getPaymentStatus());
        dto.setTotalAmount(s.getTotalAmount());
        dto.setSaleDate(s.getSaleDate());
        return dto;
    }

    private PurchaseResponseDto mapPurchaseToDto(Purchase p) {
        return new PurchaseResponseDto(
                p.getPurchaseId(),
                p.getSupplier().getSupplierName(),
                p.getInvoiceNumber(),
                p.getPurchaseDate(),
                p.getTotalAmount(),
                p.getTax(),
                p.getPaidAmount(),
                p.getPendingAmount(),
                p.getPaymentStatus(),
                p.getCreatedBy().getUsername()
        );
    }
}
