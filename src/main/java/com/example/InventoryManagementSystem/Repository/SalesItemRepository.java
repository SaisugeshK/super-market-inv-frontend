package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.SalesItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesItemRepository extends JpaRepository<SalesItem, Long> {

    List<SalesItem> findBySaleId(Long saleId);

    // Product sales report: qty sold + revenue per product
    @Query("SELECT si.productId, SUM(si.quantity), SUM(si.total) FROM SalesItem si GROUP BY si.productId")
    List<Object[]> getProductSalesSummary();
}