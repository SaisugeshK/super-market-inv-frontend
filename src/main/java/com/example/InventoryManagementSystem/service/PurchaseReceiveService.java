package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.PurchaseReceiveRequestDto;
import com.example.InventoryManagementSystem.dto.PurchaseResponseDto;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.Purchase;
import com.example.InventoryManagementSystem.model.PurchaseItem;
import com.example.InventoryManagementSystem.model.StockMovement;
import com.example.InventoryManagementSystem.model.Supplier;
import com.example.InventoryManagementSystem.model.User;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseItemRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseRepository;
import com.example.InventoryManagementSystem.Repository.StockMovementRepository;
import com.example.InventoryManagementSystem.Repository.SupplierRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Single-transaction goods-receipt. Locks each product row, computes the
 * purchase totals from the line values, adds stock and writes the purchase,
 * its lines and the stock-movement history atomically.
 */
@Service
@RequiredArgsConstructor
public class PurchaseReceiveService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final StockMovementRepository stockMovementRepository;

    private static BigDecimal money(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public PurchaseResponseDto receive(PurchaseReceiveRequestDto request) {

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        User createdBy = request.getCreatedBy() == null ? null
                : userRepository.findById(request.getCreatedBy()).orElse(null);

        BigDecimal goods = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;

        List<Product> productsToSave = new ArrayList<>();
        List<PurchaseItem> lines = new ArrayList<>();
        List<StockMovement> movements = new ArrayList<>();

        for (PurchaseReceiveRequestDto.Item item : request.getItems()) {

            Product product = productRepository.findByIdForUpdate(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            int qty = item.getQuantity();
            BigDecimal price = money(item.getPurchasePrice());
            BigDecimal lineTax = money(item.getTaxAmount());
            BigDecimal lineGoods = money(price.multiply(BigDecimal.valueOf(qty)));
            BigDecimal lineTotal = money(lineGoods.add(lineTax));

            goods = goods.add(lineGoods);
            tax = tax.add(lineTax);

            int current = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            product.setStockQuantity(current + qty);
            // keep the catalogue's latest cost in step with what was actually paid
            product.setPurchasePrice(price);
            productsToSave.add(product);

            PurchaseItem line = PurchaseItem.builder()
                    .product(product)
                    .quantity(qty)
                    .purchasePrice(price)
                    .taxAmount(lineTax)
                    .total(lineTotal)
                    .build();
            lines.add(line);

            movements.add(StockMovement.builder()
                    .product(product)
                    .movementType("PURCHASE_IN")
                    .quantity(qty)
                    .notes("Goods received")
                    .build());
        }

        BigDecimal total = money(goods.add(tax));
        BigDecimal paid = money(request.getPaidAmount());

        Purchase purchase = Purchase.builder()
                .supplier(supplier)
                .invoiceNumber(request.getInvoiceNumber())
                .totalAmount(total)
                .tax(money(tax))
                .paidAmount(paid)
                .createdBy(createdBy)
                .build();
        Purchase saved = purchaseRepository.save(purchase);

        productRepository.saveAll(productsToSave);

        for (PurchaseItem line : lines) {
            line.setPurchase(saved);
        }
        purchaseItemRepository.saveAll(lines);

        for (StockMovement m : movements) {
            m.setReferenceId(saved.getPurchaseId());
        }
        stockMovementRepository.saveAll(movements);

        return new PurchaseResponseDto(
                saved.getPurchaseId(),
                supplier.getSupplierName(),
                saved.getInvoiceNumber(),
                saved.getPurchaseDate(),
                saved.getTotalAmount(),
                saved.getTax(),
                saved.getPaidAmount(),
                saved.getPendingAmount(),
                saved.getPaymentStatus(),
                createdBy != null ? createdBy.getUsername() : null,
                supplier.getSupplierId(),
                saved.getReturnedAmount());
    }
}
