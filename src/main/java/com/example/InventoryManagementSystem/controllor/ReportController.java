package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.PurchaseResponseDto;
import com.example.InventoryManagementSystem.dto.SalesResponseDTO;
import com.example.InventoryManagementSystem.dto.StockReportItemDto;
import com.example.InventoryManagementSystem.dto.SupplierOutstandingDto;
import com.example.InventoryManagementSystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/reports/sales
     * Params: from, to (ISO datetime), counterId, paymentMethod
     */
    @GetMapping("/sales")
    public ResponseEntity<List<SalesResponseDTO>> getSalesReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long counterId,
            @RequestParam(required = false) String paymentMethod) {

        return ResponseEntity.ok(
                reportService.getSalesReport(from, to, counterId, paymentMethod)
        );
    }

    /**
     * GET /api/reports/purchases
     * Params: supplierId, from, to (ISO datetime)
     */
    @GetMapping("/purchases")
    public ResponseEntity<List<PurchaseResponseDto>> getPurchaseReport(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        return ResponseEntity.ok(
                reportService.getPurchaseReport(supplierId, from, to)
        );
    }

    /**
     * GET /api/reports/supplier-outstanding
     * Returns total purchases, paid, and pending per supplier.
     */
    @GetMapping("/supplier-outstanding")
    public ResponseEntity<List<SupplierOutstandingDto>> getSupplierOutstanding() {
        return ResponseEntity.ok(reportService.getSupplierOutstanding());
    }

    /**
     * GET /api/reports/stock
     * Returns all products with stock value and low-stock flag.
     */
    @GetMapping("/stock")
    public ResponseEntity<List<StockReportItemDto>> getStockReport() {
        return ResponseEntity.ok(reportService.getStockReport());
    }

    /**
     * GET /api/reports/product-sales
     * Returns quantity sold and revenue per product.
     */
    @GetMapping("/product-sales")
    public ResponseEntity<List<Map<String, Object>>> getProductSalesReport() {
        return ResponseEntity.ok(reportService.getProductSalesReport());
    }
}
