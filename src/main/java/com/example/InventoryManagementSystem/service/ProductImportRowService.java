package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.dto.ProductBarcodeRequestDTO;
import com.example.InventoryManagementSystem.dto.ProductRequestDTO;
import com.example.InventoryManagementSystem.dto.ProductResponseDTO;
import com.example.InventoryManagementSystem.dto.ProductTaxRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates ONE product row from a bulk import in its OWN transaction
 * (REQUIRES_NEW), so a bad row can never roll back or block the other rows
 * in the same file (spec 3e). Called from ProductImportServiceImpl, a
 * separate bean, so the @Transactional proxy actually applies.
 *
 * Category/Unit auto-create already happened (and already committed) before
 * this is called — see ProductImportServiceImpl — so it is not repeated or
 * undone here.
 *
 * Validation reuses the exact same jakarta Validator + ProductRequestDTO as
 * POST /api/products (@Valid on the controller does the same thing); nothing
 * here duplicates or diverges from those rules.
 */
@Service
@RequiredArgsConstructor
public class ProductImportRowService {

    private final ProductService productService;
    private final ProductTaxService productTaxService;
    private final ProductBarcodeService productBarcodeService;
    private final ProductRepository productRepository;
    private final Validator validator;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RowOutcome createRow(ProductRequestDTO dto, String taxName, Double taxPercentage, String altBarcode) {

        Set<ConstraintViolation<ProductRequestDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            throw new RuntimeException(msg);
        }

        if (dto.getSku() != null && !dto.getSku().isBlank() && productRepository.existsBySku(dto.getSku())) {
            throw new RuntimeException("SKU already exists: " + dto.getSku());
        }
        if (dto.getBarcode() != null && !dto.getBarcode().isBlank()
                && productRepository.existsByBarcode(dto.getBarcode())) {
            throw new RuntimeException("Barcode already exists: " + dto.getBarcode());
        }

        ProductResponseDTO created = productService.createProduct(dto);

        // Extra barcode / tax rows are best-effort, exactly like the Add Product
        // form (Products.jsx onSubmit): a failure here does not undo the product.
        StringBuilder note = new StringBuilder();

        if (altBarcode != null && !altBarcode.isBlank()) {
            try {
                ProductBarcodeRequestDTO barcodeDto = new ProductBarcodeRequestDTO();
                barcodeDto.setProductId(created.getProductId());
                barcodeDto.setBarcode(altBarcode);
                productBarcodeService.createBarcode(barcodeDto);
            } catch (Exception e) {
                note.append("additional barcode not saved (").append(e.getMessage()).append("). ");
            }
        }

        if (taxPercentage != null) {
            try {
                ProductTaxRequestDTO taxDto = new ProductTaxRequestDTO();
                taxDto.setProductId(created.getProductId());
                taxDto.setTaxName(taxName == null || taxName.isBlank() ? "GST" : taxName);
                taxDto.setTaxPercentage(taxPercentage);
                productTaxService.createTax(taxDto);
            } catch (Exception e) {
                note.append("tax not saved (").append(e.getMessage()).append("). ");
            }
        }

        return new RowOutcome(created, note.toString().trim());
    }

    public record RowOutcome(ProductResponseDTO product, String note) {
    }
}
