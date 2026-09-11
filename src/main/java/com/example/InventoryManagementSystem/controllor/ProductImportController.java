package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.ProductImportResponseDTO;
import com.example.InventoryManagementSystem.service.ProductImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Bulk product import — fully separate from ProductController (single
 * create/update/delete). Role-restricted to ADMIN in SecurityConfig.
 */
@RestController
@RequestMapping("/api/products/import")
@RequiredArgsConstructor
public class ProductImportController {

    private final ProductImportService importService;

    @PostMapping
    public ResponseEntity<ProductImportResponseDTO> importProducts(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importProducts(file));
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate(
            @RequestParam(name = "format", defaultValue = "csv") String format) {

        boolean xlsx = "xlsx".equalsIgnoreCase(format);
        byte[] body = xlsx ? importService.generateTemplateXlsx() : importService.generateTemplateCsv();
        String filename = xlsx ? "product_import_template.xlsx" : "product_import_template.csv";
        MediaType type = xlsx
                ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                : MediaType.parseMediaType("text/csv");

        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }
}
