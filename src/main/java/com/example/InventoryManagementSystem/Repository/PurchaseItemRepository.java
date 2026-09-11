package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PurchaseItemRepository
        extends JpaRepository<PurchaseItem, Long> {

    // line items for a purchase (purchase / supplier history detail)
    List<PurchaseItem> findByPurchase_PurchaseId(Long purchaseId);

    // Batch fetch for the purchase report.
    List<PurchaseItem> findByPurchase_PurchaseIdIn(Collection<Long> purchaseIds);

    boolean existsByProduct_ProductId(Long productId);
}
