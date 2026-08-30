package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.PurchaseItemRequestDto;
import com.example.InventoryManagementSystem.dto.PurchaseItemResponseDto;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.Purchase;
import com.example.InventoryManagementSystem.model.PurchaseItem;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseItemRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseItemServiceImpl implements PurchaseItemService {

    private final PurchaseItemRepository purchaseItemRepository;
    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;

    // CREATE — also increments stock
    @Override
    @Transactional
    public PurchaseItemResponseDto createPurchaseItem(PurchaseItemRequestDto request) {

        Purchase purchase = purchaseRepository.findById(request.getPurchaseId())
                .orElseThrow(() -> new RuntimeException("Purchase not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        PurchaseItem purchaseItem = PurchaseItem.builder()
                .purchase(purchase)
                .product(product)
                .quantity(request.getQuantity())
                .purchasePrice(request.getPurchasePrice())
                .taxAmount(request.getTaxAmount())
                .total(request.getTotal())
                .build();

        PurchaseItem savedPurchaseItem = purchaseItemRepository.save(purchaseItem);

        // ✅ STOCK INCREASE on purchase
        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        product.setStockQuantity(currentStock + request.getQuantity());
        productRepository.save(product);

        return mapToResponse(savedPurchaseItem);
    }

    // GET ALL
    @Override
    public List<PurchaseItemResponseDto> getAllPurchaseItems() {
        return purchaseItemRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
    @Override
    public PurchaseItemResponseDto getPurchaseItemById(Long purchaseItemId) {
        PurchaseItem purchaseItem = purchaseItemRepository.findById(purchaseItemId)
                .orElseThrow(() -> new RuntimeException("Purchase Item not found"));
        return mapToResponse(purchaseItem);
    }

    // UPDATE — reverses old stock, applies new stock
    @Override
    @Transactional
    public PurchaseItemResponseDto updatePurchaseItem(Long purchaseItemId, PurchaseItemRequestDto request) {

        PurchaseItem purchaseItem = purchaseItemRepository.findById(purchaseItemId)
                .orElseThrow(() -> new RuntimeException("Purchase Item not found"));

        // reverse old stock
        Product oldProduct = purchaseItem.getProduct();
        int oldQty = purchaseItem.getQuantity() != null ? purchaseItem.getQuantity() : 0;
        oldProduct.setStockQuantity(
                (oldProduct.getStockQuantity() != null ? oldProduct.getStockQuantity() : 0) - oldQty
        );
        productRepository.save(oldProduct);

        Purchase purchase = purchaseRepository.findById(request.getPurchaseId())
                .orElseThrow(() -> new RuntimeException("Purchase not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        purchaseItem.setPurchase(purchase);
        purchaseItem.setProduct(product);
        purchaseItem.setQuantity(request.getQuantity());
        purchaseItem.setPurchasePrice(request.getPurchasePrice());
        purchaseItem.setTaxAmount(request.getTaxAmount());
        purchaseItem.setTotal(request.getTotal());

        PurchaseItem updatedPurchaseItem = purchaseItemRepository.save(purchaseItem);

        // apply new stock
        int newStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        product.setStockQuantity(newStock + request.getQuantity());
        productRepository.save(product);

        return mapToResponse(updatedPurchaseItem);
    }

    // DELETE — also reverses stock
    @Override
    @Transactional
    public void deletePurchaseItem(Long purchaseItemId) {

        PurchaseItem purchaseItem = purchaseItemRepository.findById(purchaseItemId)
                .orElseThrow(() -> new RuntimeException("Purchase Item not found"));

        // reverse stock on delete
        Product product = purchaseItem.getProduct();
        int qty = purchaseItem.getQuantity() != null ? purchaseItem.getQuantity() : 0;
        product.setStockQuantity(
                (product.getStockQuantity() != null ? product.getStockQuantity() : 0) - qty
        );
        productRepository.save(product);

        purchaseItemRepository.delete(purchaseItem);
    }

    // MAP TO RESPONSE DTO
    private PurchaseItemResponseDto mapToResponse(PurchaseItem purchaseItem) {
        return PurchaseItemResponseDto.builder()
                .purchaseItemId(purchaseItem.getPurchaseItemId())
                .purchaseId(purchaseItem.getPurchase().getPurchaseId())
                .invoiceNumber(purchaseItem.getPurchase().getInvoiceNumber())
                .productId(purchaseItem.getProduct().getProductId())
                .productName(purchaseItem.getProduct().getProductName())
                .quantity(purchaseItem.getQuantity())
                .purchasePrice(purchaseItem.getPurchasePrice())
                .taxAmount(purchaseItem.getTaxAmount())
                .total(purchaseItem.getTotal())
                .build();
    }
}