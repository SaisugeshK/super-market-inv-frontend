package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.SalesItemRequestDTO;
import com.example.InventoryManagementSystem.dto.SalesItemResponseDTO;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.SalesItem;
import com.example.InventoryManagementSystem.model.StockMovement;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.SalesItemRepository;
import com.example.InventoryManagementSystem.Repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesItemServiceImpl implements SalesItemService {

    private final SalesItemRepository salesItemRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    // CREATE SALES ITEM  ⭐ MUST MATCH INTERFACE EXACTLY
    @Override
    public SalesItemResponseDTO createSalesItem(SalesItemRequestDTO dto) {

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < dto.getQuantity()) {
            throw new RuntimeException("Not enough stock available");
        }

        product.setStockQuantity(product.getStockQuantity() - dto.getQuantity());
        productRepository.save(product);

        BigDecimal sellingPrice = product.getSellingPrice();

        if (sellingPrice == null) {
            throw new RuntimeException("Selling price not set for product");
        }

        BigDecimal total = sellingPrice.multiply(
                BigDecimal.valueOf(dto.getQuantity())
        );

        SalesItem item = new SalesItem();
        item.setSaleId(dto.getSaleId());
        item.setProductId(dto.getProductId());
        item.setQuantity(dto.getQuantity());
        item.setSellingPrice(sellingPrice);
        item.setTotal(total);

        SalesItem saved = salesItemRepository.save(item);

        // record stock movement (history / traceability)
        stockMovementRepository.save(StockMovement.builder()
                .product(product)
                .movementType("SALE_OUT")
                .quantity(dto.getQuantity())
                .referenceId(dto.getSaleId() != null ? Math.toIntExact(dto.getSaleId()) : null)
                .notes("Stock deducted from sale " + dto.getSaleId())
                .build());

        return mapToDTO(saved);
    }

    // GET ALL
    @Override
    public List<SalesItemResponseDTO> getAllSalesItems() {

        return salesItemRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // GET BY ID
    @Override
    public SalesItemResponseDTO getSalesItemById(Long id) {

        SalesItem item = salesItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales item not found"));

        return mapToDTO(item);
    }

    @Override
    public SalesItemResponseDTO updateSalesItem(Long id, SalesItemRequestDTO dto) {

        // FIND EXISTING ITEM
        SalesItem item = salesItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales item not found"));

        // RESTORE OLD STOCK
        Product oldProduct = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        oldProduct.setStockQuantity(
                oldProduct.getStockQuantity() + item.getQuantity()
        );
        productRepository.save(oldProduct);

        // GET NEW PRODUCT
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // STOCK CHECK
        if (product.getStockQuantity() < dto.getQuantity()) {
            throw new RuntimeException("Not enough stock available");
        }

        // REDUCE NEW STOCK
        product.setStockQuantity(
                product.getStockQuantity() - dto.getQuantity()
        );
        productRepository.save(product);

        // CALCULATE TOTAL
        BigDecimal price = product.getSellingPrice();

        if (price == null) {
            throw new RuntimeException("Selling price not set for product");
        }

        BigDecimal total = price.multiply(
                BigDecimal.valueOf(dto.getQuantity())
        );

        // UPDATE ENTITY
        item.setProductId(dto.getProductId());
        item.setQuantity(dto.getQuantity());
        item.setSellingPrice(price);
        item.setTotal(total);

        SalesItem updated = salesItemRepository.save(item);

        // RESPONSE
        SalesItemResponseDTO response = new SalesItemResponseDTO();
        response.setSaleItemId(updated.getSaleItemId());
        response.setSaleId(updated.getSaleId());
        response.setProductId(updated.getProductId());
        response.setQuantity(updated.getQuantity());
        response.setSellingPrice(updated.getSellingPrice());
        response.setTotal(updated.getTotal());

        return response;
    }

    // GET BY SALE ID
    @Override
    public List<SalesItemResponseDTO> getItemsBySaleId(Long saleId) {

        return salesItemRepository.findBySaleId(saleId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // DELETE
    @Override
    public void deleteSalesItem(Long id) {

        SalesItem item = salesItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales item not found"));

        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        productRepository.save(product);

        salesItemRepository.delete(item);
    }

    // MAPPER
    private SalesItemResponseDTO mapToDTO(SalesItem item) {

        SalesItemResponseDTO dto = new SalesItemResponseDTO();

        dto.setSaleItemId(item.getSaleItemId());
        dto.setSaleId(item.getSaleId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setSellingPrice(item.getSellingPrice());
        dto.setTotal(item.getTotal());

        return dto;
    }
}