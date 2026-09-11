package com.example.InventoryManagementSystem.service;


import com.example.InventoryManagementSystem.dto.ProductBarcodeRequestDTO;
import com.example.InventoryManagementSystem.dto.ProductBarcodeResponseDTO;
import com.example.InventoryManagementSystem.model.ProductBarcode;
import com.example.InventoryManagementSystem.Repository.ProductBarcodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductBarcodeServiceImpl implements ProductBarcodeService {

    private final ProductBarcodeRepository repository;

    @Override
    public ProductBarcodeResponseDTO createBarcode(ProductBarcodeRequestDTO request) {

        ProductBarcode barcode = ProductBarcode.builder()
                .productId(request.getProductId())
                .barcode(request.getBarcode())
                .build();

        repository.save(barcode);

        return mapToDTO(barcode);
    }

    @Override
    public List<ProductBarcodeResponseDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductBarcodeResponseDTO updateBarcode(Long id, ProductBarcodeRequestDTO request) {

        ProductBarcode barcode = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barcode not found"));

        barcode.setProductId(request.getProductId());
        barcode.setBarcode(request.getBarcode());

        repository.save(barcode);

        return mapToDTO(barcode);
    }

    @Override
    public List<ProductBarcodeResponseDTO> getByProductId(Long productId) {
        return repository.findByProductId(productId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductBarcodeResponseDTO getByBarcode(String barcode) {

        ProductBarcode entity = repository.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Barcode not found"));

        return mapToDTO(entity);
    }

    @Override
    public void deleteBarcode(Long barcodeId) {
        repository.deleteById(barcodeId);
    }

    private ProductBarcodeResponseDTO mapToDTO(ProductBarcode entity) {

        ProductBarcodeResponseDTO dto = new ProductBarcodeResponseDTO();
        dto.setBarcodeId(entity.getBarcodeId());
        dto.setProductId(entity.getProductId());
        dto.setBarcode(entity.getBarcode());
        return dto;
    }
}