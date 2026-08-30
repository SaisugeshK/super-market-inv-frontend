package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseRepository;
import com.example.InventoryManagementSystem.Repository.SalesRepository;
import com.example.InventoryManagementSystem.dto.DashboardSummaryDto;
import com.example.InventoryManagementSystem.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SalesRepository salesRepository;
    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;

    public DashboardSummaryDto getSummary() {

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        // today's totals
        BigDecimal todaySales = salesRepository.getTodayTotalSales(startOfDay);
        long todayBills = salesRepository.getTodayBillCount(startOfDay);

        // overall purchase total
        BigDecimal totalPurchase = purchaseRepository.findAll()
                .stream()
                .map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // total supplier pending
        BigDecimal totalPending = purchaseRepository.findAll()
                .stream()
                .map(p -> p.getPendingAmount() != null ? p.getPendingAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // low stock products
        List<Product> lowStockProducts = productRepository.findLowStockProducts();

        List<DashboardSummaryDto.LowStockItem> lowStockItems = lowStockProducts
                .stream()
                .map(p -> DashboardSummaryDto.LowStockItem.builder()
                        .productId(p.getProductId())
                        .productName(p.getProductName())
                        .stockQuantity(p.getStockQuantity())
                        .minimumStock(p.getMinimumStock())
                        .build())
                .collect(Collectors.toList());

        return DashboardSummaryDto.builder()
                .todayTotalSales(todaySales != null ? todaySales : BigDecimal.ZERO)
                .todayBillCount(todayBills)
                .totalPurchaseAmount(totalPurchase)
                .totalSupplierPending(totalPending)
                .lowStockProductCount(lowStockItems.size())
                .lowStockItems(lowStockItems)
                .build();
    }
}
