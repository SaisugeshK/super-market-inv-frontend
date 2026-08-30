package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseItemRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseRepository;
import com.example.InventoryManagementSystem.Repository.SalesItemRepository;
import com.example.InventoryManagementSystem.Repository.SalesRepository;
import com.example.InventoryManagementSystem.dto.PurchaseReportRowDto;
import com.example.InventoryManagementSystem.dto.SalesReportRowDto;
import com.example.InventoryManagementSystem.dto.StockReportItemDto;
import com.example.InventoryManagementSystem.dto.SupplierOutstandingDto;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.Purchase;
import com.example.InventoryManagementSystem.model.PurchaseItem;
import com.example.InventoryManagementSystem.model.Sales;
import com.example.InventoryManagementSystem.model.SalesItem;
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
    private final SalesItemRepository salesItemRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    // ─── SALES REPORT ────────────────────────────────────────────────────────────
    public List<SalesReportRowDto> getSalesReport(
            LocalDateTime from,
            LocalDateTime to,
            Long counterId,
            String paymentMethod,
            String invoiceNumber) {

        List<Sales> sales;

        if (from != null && to != null) {
            sales = salesRepository.findBySaleDateBetween(from, to);
        } else {
            sales = salesRepository.findAll();
        }

        return sales.stream()
                .filter(s -> counterId == null || counterId.equals(s.getCounterId()))
                .filter(s -> paymentMethod == null || paymentMethod.equalsIgnoreCase(s.getPaymentMethod()))
                .filter(s -> invoiceNumber == null || invoiceNumber.equalsIgnoreCase(s.getInvoiceNumber()))
                .map(this::mapSalesRow)
                .collect(Collectors.toList());
    }

    // ─── PURCHASE REPORT ─────────────────────────────────────────────────────────
    public List<PurchaseReportRowDto> getPurchaseReport(
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
                .map(this::mapPurchaseRow)
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
    public List<java.util.Map<String, Object>> getProductSalesReport(LocalDateTime from, LocalDateTime to) {

        List<Object[]> rows = (from != null || to != null)
                ? salesItemRepository.getProductSalesSummaryBetween(from, to)
                : salesItemRepository.getProductSalesSummary();

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

    private SalesReportRowDto mapSalesRow(Sales s) {

        String customerName = s.getCustomerId() == null ? null
                : customerRepository.findById(s.getCustomerId())
                        .map(c -> c.getCustomerName())
                        .orElse(null);

        List<SalesReportRowDto.Line> lines = salesItemRepository.findBySaleId(s.getSaleId())
                .stream()
                .map(this::mapSalesLine)
                .collect(Collectors.toList());

        return SalesReportRowDto.builder()
                .saleId(s.getSaleId())
                .invoiceNumber(s.getInvoiceNumber())
                .saleDate(s.getSaleDate())
                .customerId(s.getCustomerId())
                .customerName(customerName)
                .counterId(s.getCounterId())
                .paymentMethod(s.getPaymentMethod())
                .paymentStatus(s.getPaymentStatus())
                .totalAmount(s.getTotalAmount())
                .items(lines)
                .build();
    }

    private SalesReportRowDto.Line mapSalesLine(SalesItem si) {
        String productName = productRepository.findById(si.getProductId())
                .map(Product::getProductName)
                .orElse("Unknown");
        return SalesReportRowDto.Line.builder()
                .productId(si.getProductId())
                .productName(productName)
                .quantity(si.getQuantity())
                .sellingPrice(si.getSellingPrice())
                .total(si.getTotal())
                .build();
    }

    private PurchaseReportRowDto mapPurchaseRow(Purchase p) {

        List<PurchaseReportRowDto.Line> lines = purchaseItemRepository
                .findByPurchase_PurchaseId(p.getPurchaseId())
                .stream()
                .map(this::mapPurchaseLine)
                .collect(Collectors.toList());

        return PurchaseReportRowDto.builder()
                .purchaseId(p.getPurchaseId())
                .supplierName(p.getSupplier() != null ? p.getSupplier().getSupplierName() : null)
                .invoiceNumber(p.getInvoiceNumber())
                .purchaseDate(p.getPurchaseDate())
                .totalAmount(p.getTotalAmount())
                .tax(p.getTax())
                .paidAmount(p.getPaidAmount())
                .pendingAmount(p.getPendingAmount())
                .paymentStatus(p.getPaymentStatus())
                .createdBy(p.getCreatedBy() != null ? p.getCreatedBy().getUsername() : null)
                .items(lines)
                .build();
    }

    private PurchaseReportRowDto.Line mapPurchaseLine(PurchaseItem pi) {
        Product product = pi.getProduct();
        return PurchaseReportRowDto.Line.builder()
                .productId(product != null ? product.getProductId() : null)
                .productName(product != null ? product.getProductName() : "Unknown")
                .quantity(pi.getQuantity())
                .unit(product != null ? product.getUnit() : null)
                .purchasePrice(pi.getPurchasePrice())
                .taxAmount(pi.getTaxAmount())
                .total(pi.getTotal())
                .build();
    }
}
