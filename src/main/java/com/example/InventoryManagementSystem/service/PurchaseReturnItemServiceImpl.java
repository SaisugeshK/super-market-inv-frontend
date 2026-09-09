package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.PurchaseReturnItemRequestDTO;
import com.example.InventoryManagementSystem.dto.PurchaseReturnItemResponseDTO;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseReturnItemRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseReturnRepository;
import com.example.InventoryManagementSystem.Repository.StockMovementRepository;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.Purchase;
import com.example.InventoryManagementSystem.model.PurchaseReturn;
import com.example.InventoryManagementSystem.model.PurchaseReturnItem;
import com.example.InventoryManagementSystem.model.StockMovement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseReturnItemServiceImpl
        implements PurchaseReturnItemService {

    private final PurchaseReturnItemRepository repository;
    private final PurchaseReturnRepository purchaseReturnRepository;
    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    // CREATE — sends goods back to the supplier:
    //  • decrements product stock
    //  • logs a RETURN_OUT stock movement
    //  • increases the parent purchase's returnedAmount (lowers what you owe)
    @Override
    @Transactional
    public PurchaseReturnItemResponseDTO createPurchaseReturnItem(
            PurchaseReturnItemRequestDTO requestDTO) {

        PurchaseReturn purchaseReturn = purchaseReturnRepository
                .findById(requestDTO.getPurchaseReturnId())
                .orElseThrow(() -> new RuntimeException("Purchase return not found"));

        Product product = productRepository
                .findById(requestDTO.getProductId().longValue())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int qty = requestDTO.getQuantity() != null ? requestDTO.getQuantity() : 0;
        BigDecimal total = requestDTO.getPrice()
                .multiply(BigDecimal.valueOf(qty));

        PurchaseReturnItem entity = PurchaseReturnItem.builder()
                .purchaseReturnId(requestDTO.getPurchaseReturnId())
                .productId(requestDTO.getProductId())
                .quantity(qty)
                .price(requestDTO.getPrice())
                .total(total)
                .build();

        PurchaseReturnItem saved = repository.save(entity);

        // stock OUT
        adjustStock(product, -qty);
        stockMovementRepository.save(StockMovement.builder()
                .product(product)
                .movementType("RETURN_OUT")
                .quantity(qty)
                .referenceId(requestDTO.getPurchaseReturnId() == null ? null
                        : requestDTO.getPurchaseReturnId().longValue())
                .notes("Goods returned to supplier (purchase return "
                        + requestDTO.getPurchaseReturnId() + ")")
                .build());

        // reduce supplier payable on the linked purchase
        adjustPurchaseReturned(purchaseReturn.getPurchaseId(), total);

        return mapToResponse(saved);
    }

    @Override
    public PurchaseReturnItemResponseDTO getPurchaseReturnItemById(Integer id) {
        PurchaseReturnItem entity = repository.findById(id).orElse(null);
        return entity == null ? null : mapToResponse(entity);
    }

    @Override
    public List<PurchaseReturnItemResponseDTO> getAllPurchaseReturnItems() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // UPDATE — reverse the old line's effect, then apply the new one
    @Override
    @Transactional
    public PurchaseReturnItemResponseDTO updatePurchaseReturnItem(
            Integer id,
            PurchaseReturnItemRequestDTO requestDTO) {

        PurchaseReturnItem entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return null;
        }

        // --- reverse old ---
        Product oldProduct = productRepository
                .findById(entity.getProductId().longValue())
                .orElse(null);
        int oldQty = entity.getQuantity() != null ? entity.getQuantity() : 0;
        BigDecimal oldTotal = entity.getTotal() != null ? entity.getTotal() : BigDecimal.ZERO;
        if (oldProduct != null) adjustStock(oldProduct, oldQty);

        PurchaseReturn purchaseReturn = purchaseReturnRepository
                .findById(entity.getPurchaseReturnId())
                .orElseThrow(() -> new RuntimeException("Purchase return not found"));
        adjustPurchaseReturned(purchaseReturn.getPurchaseId(), oldTotal.negate());

        // --- apply new ---
        Product product = productRepository
                .findById(requestDTO.getProductId().longValue())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        int qty = requestDTO.getQuantity() != null ? requestDTO.getQuantity() : 0;
        BigDecimal total = requestDTO.getPrice().multiply(BigDecimal.valueOf(qty));

        entity.setProductId(requestDTO.getProductId());
        entity.setQuantity(qty);
        entity.setPrice(requestDTO.getPrice());
        entity.setTotal(total);
        PurchaseReturnItem updated = repository.save(entity);

        adjustStock(product, -qty);
        adjustPurchaseReturned(purchaseReturn.getPurchaseId(), total);

        return mapToResponse(updated);
    }

    // DELETE — undoes the return line: stock comes back, payable goes back up
    @Override
    @Transactional
    public void deletePurchaseReturnItem(Integer id) {

        PurchaseReturnItem entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return;
        }

        int qty = entity.getQuantity() != null ? entity.getQuantity() : 0;
        BigDecimal total = entity.getTotal() != null ? entity.getTotal() : BigDecimal.ZERO;

        productRepository.findById(entity.getProductId().longValue())
                .ifPresent(product -> {
                    adjustStock(product, qty);
                    stockMovementRepository.save(StockMovement.builder()
                            .product(product)
                            .movementType("RETURN_IN")
                            .quantity(qty)
                            .referenceId(entity.getPurchaseReturnId() == null ? null
                                    : entity.getPurchaseReturnId().longValue())
                            .notes("Purchase-return line reversed")
                            .build());
                });

        purchaseReturnRepository.findById(entity.getPurchaseReturnId())
                .ifPresent(pr -> adjustPurchaseReturned(pr.getPurchaseId(), total.negate()));

        repository.delete(entity);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private void adjustStock(Product product, int delta) {
        int current = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        product.setStockQuantity(current + delta);
        productRepository.save(product);
    }

    private void adjustPurchaseReturned(Integer purchaseId, BigDecimal delta) {
        if (purchaseId == null) return;
        Purchase purchase = purchaseRepository.findById(purchaseId.longValue()).orElse(null);
        if (purchase == null) return;
        BigDecimal current = purchase.getReturnedAmount() != null
                ? purchase.getReturnedAmount() : BigDecimal.ZERO;
        BigDecimal next = current.add(delta);
        if (next.compareTo(BigDecimal.ZERO) < 0) next = BigDecimal.ZERO;
        purchase.setReturnedAmount(next);
        purchaseRepository.save(purchase); // @PreUpdate recomputes pendingAmount + status
    }

    private PurchaseReturnItemResponseDTO mapToResponse(PurchaseReturnItem entity) {
        return PurchaseReturnItemResponseDTO.builder()
                .purchaseReturnItemId(entity.getPurchaseReturnItemId())
                .purchaseReturnId(entity.getPurchaseReturnId())
                .productId(entity.getProductId())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .total(entity.getTotal())
                .build();
    }
}
