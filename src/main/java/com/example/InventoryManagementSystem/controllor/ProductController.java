package com.example.InventoryManagementSystem.controllor;


import com.example.InventoryManagementSystem.dto.ProductRequestDTO;
import com.example.InventoryManagementSystem.dto.ProductResponseDTO;
import com.example.InventoryManagementSystem.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO dto) {
        return ResponseEntity.ok(service.createProduct(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllProducts());
    }

    // Barcode-based billing: scan a barcode, get the product
    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<ProductResponseDTO> getByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(service.getProductByBarcode(barcode));
    }

    // Fast billing: type-to-search by name or barcode
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> search(@RequestParam(name = "q", required = false) String q) {
        return ResponseEntity.ok(service.searchProducts(q));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO dto) {
        return ResponseEntity.ok(service.updateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }
}