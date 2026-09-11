package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseItemRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.dto.PurchaseRequestDto;
import com.example.InventoryManagementSystem.dto.PurchaseResponseDto;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.Purchase;
import com.example.InventoryManagementSystem.model.PurchaseItem;
import com.example.InventoryManagementSystem.model.Supplier;
import com.example.InventoryManagementSystem.model.User;
import com.example.InventoryManagementSystem.Repository.SupplierRepository;
import com.example.InventoryManagementSystem.Repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    // CREATE PURCHASE
    @Override
    public PurchaseResponseDto createPurchase(PurchaseRequestDto dto) {

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        User createdBy = userRepository.findById(dto.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal paid = dto.getPaidAmount() != null ? dto.getPaidAmount() : BigDecimal.ZERO;

        Purchase purchase = Purchase.builder()
                .supplier(supplier)
                .invoiceNumber(dto.getInvoiceNumber())
                .totalAmount(dto.getTotalAmount())
                .tax(dto.getTax())
                .paidAmount(paid)
                .createdBy(createdBy)
                .build();
        // pendingAmount + paymentStatus auto-derived in @PrePersist

        Purchase saved = purchaseRepository.save(purchase);

        return mapToDto(saved);
    }

    // GET ALL PURCHASES
    @Override
    public List<PurchaseResponseDto> getAllPurchases() {
        return purchaseRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // GET PURCHASE BY ID
    @Override
    public PurchaseResponseDto getPurchaseById(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));
        return mapToDto(purchase);
    }

    // UPDATE PURCHASE
    @Override
    public PurchaseResponseDto updatePurchase(Long id, PurchaseRequestDto dto) {

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        User createdBy = userRepository.findById(dto.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("User not found"));

        purchase.setSupplier(supplier);
        purchase.setInvoiceNumber(dto.getInvoiceNumber());
        purchase.setTotalAmount(dto.getTotalAmount());
        purchase.setTax(dto.getTax());
        purchase.setPaidAmount(dto.getPaidAmount() != null ? dto.getPaidAmount() : BigDecimal.ZERO);
        purchase.setCreatedBy(createdBy);
        // pendingAmount + paymentStatus auto-derived in @PreUpdate

        Purchase updated = purchaseRepository.save(purchase);

        return mapToDto(updated);
    }

    // DELETE PURCHASE — also removes its line items and reverses the stock they added
    @Override
    @Transactional
    public void deletePurchase(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));

        List<PurchaseItem> items = purchaseItemRepository.findByPurchase_PurchaseId(id);
        for (PurchaseItem item : items) {
            Product product = item.getProduct();
            if (product != null) {
                int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                int current = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                product.setStockQuantity(current - qty);
                productRepository.save(product);
            }
        }
        purchaseItemRepository.deleteAll(items);

        // Same class of gap as sales (BUG-036): the PURCHASE_IN movement this
        // receipt created carries a plain reference_id, not a mapped relation,
        // so it won't cascade — remove it explicitly or it becomes an orphan.
        stockMovementRepository.deleteAll(
                stockMovementRepository.findByMovementTypeAndReferenceId("PURCHASE_IN", id));

        purchaseRepository.delete(purchase);
    }

    // MAP ENTITY TO DTO
    private PurchaseResponseDto mapToDto(Purchase purchase) {
        return new PurchaseResponseDto(
                purchase.getPurchaseId(),
                purchase.getSupplier() != null ? purchase.getSupplier().getSupplierName() : null,
                purchase.getInvoiceNumber(),
                purchase.getPurchaseDate(),
                purchase.getTotalAmount(),
                purchase.getTax(),
                purchase.getPaidAmount(),
                purchase.getPendingAmount(),
                purchase.getPaymentStatus(),
                purchase.getCreatedBy() != null ? purchase.getCreatedBy().getUsername() : null,
                purchase.getSupplier() != null ? purchase.getSupplier().getSupplierId() : null,
                purchase.getReturnedAmount()
        );
    }
}