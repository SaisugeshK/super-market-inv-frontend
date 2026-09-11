package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.ProductImportResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ProductImportService {

    ProductImportResponseDTO importProducts(MultipartFile file);

    byte[] generateTemplateCsv();

    byte[] generateTemplateXlsx();
}
