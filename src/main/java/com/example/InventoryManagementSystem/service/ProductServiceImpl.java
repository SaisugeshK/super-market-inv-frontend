package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.ProductRequestDTO;
import com.example.InventoryManagementSystem.dto.ProductResponseDTO;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.Repository.ProductBarcodeRepository;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductBarcodeRepository barcodeRepository;

    public ProductServiceImpl(ProductRepository repository,
                              ProductBarcodeRepository barcodeRepository) {
        this.repository = repository;
        this.barcodeRepository = barcodeRepository;
    }

    // =======================
    // ENTITY → DTO MAPPER
    // =======================
    private ProductResponseDTO mapToDTO(Product p) {

        ProductResponseDTO dto = new ProductResponseDTO();

        dto.setProductId(p.getProductId());
        dto.setCategoryId(p.getCategoryId());
        dto.setProductName(p.getProductName());
        dto.setSku(p.getSku());
        dto.setBarcode(p.getBarcode());
        dto.setPurchasePrice(p.getPurchasePrice());
        dto.setSellingPrice(p.getSellingPrice());
        dto.setStockQuantity(p.getStockQuantity());
        dto.setMinimumStock(p.getMinimumStock());
        dto.setUnit(p.getUnit());
        dto.setStatus(p.getStatus());
        dto.setCreatedAt(p.getCreatedAt());

        return dto;
    }

    // =======================
    // CREATE PRODUCT
    // =======================
    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {

        if (dto.getStockQuantity() != null && dto.getStockQuantity() < 0) {
            throw new RuntimeException("Stock cannot be negative");
        }

        Product p = new Product();

        p.setCategoryId(dto.getCategoryId());
        p.setProductName(dto.getProductName());
        p.setSku(dto.getSku());
        p.setBarcode(dto.getBarcode());
        p.setPurchasePrice(dto.getPurchasePrice());
        p.setSellingPrice(dto.getSellingPrice());
        p.setStockQuantity(dto.getStockQuantity());
        p.setMinimumStock(dto.getMinimumStock());
        p.setUnit(dto.getUnit());
        p.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");

        return mapToDTO(repository.save(p));
    }

    // =======================
    // GET BY ID
    // =======================
    @Override
    public ProductResponseDTO getProductById(Long id) {

        Product p = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return mapToDTO(p);
    }

    // =======================
    // GET BY BARCODE  (barcode-based billing)
    // =======================
    @Override
    public ProductResponseDTO getProductByBarcode(String barcode) {

        Product p = repository.findByBarcode(barcode)
                .orElseGet(() -> barcodeRepository.findByBarcode(barcode)
                        .flatMap(pb -> repository.findById(pb.getProductId()))
                        .orElseThrow(() -> new RuntimeException(
                                "No product found for barcode: " + barcode)));

        return mapToDTO(p);
    }

    // =======================
    // SEARCH  (type-to-search fast billing)
    // =======================
    @Override
    public List<ProductResponseDTO> searchProducts(String term) {

        if (term == null || term.isBlank()) {
            return getAllProducts();
        }

        return repository.searchByNameOrBarcode(term.trim())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // =======================
    // GET ALL
    // =======================
    @Override
    public List<ProductResponseDTO> getAllProducts() {

        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // =======================
    // UPDATE PRODUCT
    // =======================
    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {

        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (dto.getCategoryId() != null)
            product.setCategoryId(dto.getCategoryId());

        if (dto.getProductName() != null)
            product.setProductName(dto.getProductName());

        if (dto.getSku() != null)
            product.setSku(dto.getSku());

        if (dto.getBarcode() != null)
            product.setBarcode(dto.getBarcode());

        if (dto.getPurchasePrice() != null)
            product.setPurchasePrice(dto.getPurchasePrice());

        if (dto.getSellingPrice() != null)
            product.setSellingPrice(dto.getSellingPrice());

        if (dto.getStockQuantity() != null) {

            if (dto.getStockQuantity() < 0) {
                throw new RuntimeException("Stock cannot be negative");
            }

            product.setStockQuantity(dto.getStockQuantity());
        }

        if (dto.getMinimumStock() != null)
            product.setMinimumStock(dto.getMinimumStock());

        if (dto.getUnit() != null)
            product.setUnit(dto.getUnit());

        if (dto.getStatus() != null)
            product.setStatus(dto.getStatus());

        return mapToDTO(repository.save(product));
    }

    // =======================
    // DELETE PRODUCT
    // =======================
    @Override
    @Transactional
    public void deleteProduct(Long id) {

        Product p = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        repository.delete(p);
    }
}