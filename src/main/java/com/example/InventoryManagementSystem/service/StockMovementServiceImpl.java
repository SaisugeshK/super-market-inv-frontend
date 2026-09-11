package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.StockMovementRequest;
import com.example.InventoryManagementSystem.dto.StockMovementResponse;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.StockMovement;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl
        implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    @Override
    public StockMovementResponse createStockMovement(
            StockMovementRequest request) {

        Product product = productRepository.findById(
                        request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"));

        StockMovement stockMovement = StockMovement.builder()
                .product(product)
                .movementType(request.getMovementType())
                .quantity(request.getQuantity())
                .referenceId(request.getReferenceId())
                .notes(request.getNotes())
                .build();

        StockMovement saved =
                stockMovementRepository.save(stockMovement);

        return mapToResponse(saved);
    }

    @Override
    public StockMovementResponse getStockMovementById(
            Long movementId) {

        StockMovement stockMovement =
                stockMovementRepository.findById(movementId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Stock movement not found"));

        return mapToResponse(stockMovement);
    }

    @Override
    public List<StockMovementResponse>
    getAllStockMovements() {

        return stockMovementRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<StockMovementResponse> getByProductId(
            Long productId) {

        return stockMovementRepository
                .findByProduct_ProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deleteStockMovement(Long movementId) {

        StockMovement stockMovement =
                stockMovementRepository.findById(movementId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Stock movement not found"));

        stockMovementRepository.delete(stockMovement);
    }

    private StockMovementResponse mapToResponse(
            StockMovement stockMovement) {

        return StockMovementResponse.builder()
                .movementId(stockMovement.getMovementId())
                .productId(stockMovement.getProduct()
                        .getProductId())
                .productName(stockMovement.getProduct()
                        .getProductName())
                .movementType(stockMovement.getMovementType())
                .quantity(stockMovement.getQuantity())
                .referenceId(stockMovement.getReferenceId())
                .notes(stockMovement.getNotes())
                .createdAt(stockMovement.getCreatedAt())
                .build();
    }
}