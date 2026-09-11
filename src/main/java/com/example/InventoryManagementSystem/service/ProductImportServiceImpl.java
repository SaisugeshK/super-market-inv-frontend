package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.CategoryRepository;
import com.example.InventoryManagementSystem.Repository.UnitRepository;
import com.example.InventoryManagementSystem.dto.CategoryRequest;
import com.example.InventoryManagementSystem.dto.CategoryResponse;
import com.example.InventoryManagementSystem.dto.ProductImportResponseDTO;
import com.example.InventoryManagementSystem.dto.ProductImportRowResultDTO;
import com.example.InventoryManagementSystem.dto.ProductRequestDTO;
import com.example.InventoryManagementSystem.dto.UnitRequestDto;
import com.example.InventoryManagementSystem.dto.UnitResponseDto;
import com.example.InventoryManagementSystem.model.Category;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Bulk product import (POST /api/products/import + the template download).
 * Fully separate from ProductController/ProductServiceImpl — POST /api/products
 * (single create) is never touched. Shared logic (validation, product
 * creation, category/unit lookups) is reused via ProductService,
 * CategoryService, UnitService and ProductImportRowService rather than
 * re-implemented here.
 */
@Service
@RequiredArgsConstructor
public class ProductImportServiceImpl implements ProductImportService {

    // Exact headers required by the spec, matching the Add Product form fields.
    private static final String[] TEMPLATE_HEADERS = {
            "Category", "Product Name", "SKU", "Barcode", "Purchase Price",
            "Selling Price", "Stock Quantity", "Minimum Stock", "Unit", "Status",
            "Tax Name", "GST/Tax %", "Additional Barcode"
    };

    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final CategoryService categoryService;
    private final UnitService unitService;
    private final ProductImportRowService rowService;

    // =======================
    // TEMPLATE DOWNLOAD
    // =======================
    @Override
    public byte[] generateTemplateCsv() {
        StringWriter sw = new StringWriter();
        try (org.apache.commons.csv.CSVPrinter printer = new org.apache.commons.csv.CSVPrinter(
                sw, CSVFormat.DEFAULT.builder().setHeader(TEMPLATE_HEADERS).build())) {
            printer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Could not generate the import template");
        }
        return sw.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] generateTemplateXlsx() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Products");
            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.length; i++) {
                header.createCell(i).setCellValue(TEMPLATE_HEADERS[i]);
            }
            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Could not generate the import template");
        }
    }

    // =======================
    // IMPORT
    // =======================
    @Override
    public ProductImportResponseDTO importProducts(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Invalid file: the uploaded file is empty");
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        boolean isCsv = filename.endsWith(".csv");
        boolean isXlsx = filename.endsWith(".xlsx");
        if (!isCsv && !isXlsx) {
            throw new RuntimeException("Invalid file type: only .csv and .xlsx files are supported");
        }

        List<RawRow> rawRows;
        try {
            rawRows = isCsv ? parseCsv(file.getInputStream()) : parseXlsx(file.getInputStream());
        } catch (Exception e) {
            // Any parse failure — a checked IOException, or a POI RuntimeException
            // for a corrupt/non-spreadsheet payload — must land as a clean 400,
            // never a raw 500. Header-validation messages (thrown below, in
            // requireHeaders) already read fine to a user, so pass those through
            // verbatim instead of re-wrapping them.
            String msg = e.getMessage();
            if (msg != null && (msg.startsWith("Missing required column") || msg.startsWith("Invalid file"))) {
                throw new RuntimeException(msg);
            }
            throw new RuntimeException("Invalid file: could not be read as a " + (isCsv ? "CSV" : "XLSX") + " file");
        }

        List<ProductImportRowResultDTO> results = new ArrayList<>();
        int succeeded = 0;
        int failed = 0;

        // Case-insensitive name -> id/code caches, shared across the whole file so
        // the same new Category/Unit name is only auto-created once per upload.
        Map<String, Long> categoryCache = new LinkedHashMap<>();
        Map<String, String> unitCache = new LinkedHashMap<>();
        // File-internal SKU/barcode dedupe: value (lowercased) -> first row number that used it.
        Map<String, Integer> seenSkus = new LinkedHashMap<>();
        Map<String, Integer> seenBarcodes = new LinkedHashMap<>();

        for (RawRow raw : rawRows) {
            if (isBlankRow(raw.values())) {
                continue; // trailing blank line — not a real row, not counted
            }

            String productNameRaw = trimOrNull(raw.values().get("Product Name"));
            List<String> preErrors = new ArrayList<>();

            BigDecimal purchasePrice = parseDecimal(raw.values().get("Purchase Price"), "Purchase Price", preErrors);
            BigDecimal sellingPrice = parseDecimal(raw.values().get("Selling Price"), "Selling Price", preErrors);
            Integer stockQuantity = parseInt(raw.values().get("Stock Quantity"), "Stock Quantity", preErrors);
            Integer minimumStock = parseInt(raw.values().get("Minimum Stock"), "Minimum Stock", preErrors);
            Double gstPercentage = parseDouble(raw.values().get("GST/Tax %"), "GST/Tax %", preErrors);

            if (!preErrors.isEmpty()) {
                results.add(failure(raw.rowNumber(), productNameRaw, String.join("; ", preErrors)));
                failed++;
                continue;
            }

            List<String> notes = new ArrayList<>();
            Long categoryId;
            String unitCode;
            try {
                categoryId = resolveCategoryId(raw.values().get("Category"), categoryCache, notes);
                unitCode = resolveUnit(raw.values().get("Unit"), unitCache, notes);
            } catch (Exception e) {
                results.add(failure(raw.rowNumber(), productNameRaw, "Could not resolve Category/Unit: " + e.getMessage()));
                failed++;
                continue;
            }

            ProductRequestDTO dto = new ProductRequestDTO();
            dto.setCategoryId(categoryId);
            dto.setProductName(productNameRaw);
            dto.setSku(trimOrNull(raw.values().get("SKU")));
            dto.setBarcode(trimOrNull(raw.values().get("Barcode")));
            dto.setPurchasePrice(purchasePrice);
            dto.setSellingPrice(sellingPrice);
            dto.setStockQuantity(stockQuantity);
            dto.setMinimumStock(minimumStock);
            dto.setUnit(unitCode);
            String statusRaw = trimOrNull(raw.values().get("Status"));
            dto.setStatus(statusRaw == null ? "Active" : statusRaw); // spec 3d: default when blank

            String dupError = checkFileDuplicates(dto, raw.rowNumber(), seenSkus, seenBarcodes);
            if (dupError != null) {
                results.add(failure(raw.rowNumber(), productNameRaw, dupError));
                failed++;
                continue;
            }

            String taxName = trimOrNull(raw.values().get("Tax Name"));
            String altBarcode = trimOrNull(raw.values().get("Additional Barcode"));

            try {
                ProductImportRowService.RowOutcome outcome =
                        rowService.createRow(dto, taxName, gstPercentage, altBarcode);

                StringBuilder message = new StringBuilder("Created");
                if (!notes.isEmpty()) {
                    message.append(" (").append(String.join("; ", notes)).append(")");
                }
                if (!outcome.note().isEmpty()) {
                    message.append(" — ").append(outcome.note());
                }

                results.add(ProductImportRowResultDTO.builder()
                        .rowNumber(raw.rowNumber())
                        .productName(productNameRaw)
                        .status("created")
                        .message(message.toString())
                        .productId(outcome.product().getProductId())
                        .build());
                succeeded++;
            } catch (Exception e) {
                results.add(failure(raw.rowNumber(), productNameRaw, e.getMessage()));
                failed++;
            }
        }

        return ProductImportResponseDTO.builder()
                .totalRows(results.size())
                .succeeded(succeeded)
                .failed(failed)
                .rows(results)
                .build();
    }

    // =======================
    // CATEGORY / UNIT RESOLUTION (find case-insensitively, else auto-create)
    // =======================
    private Long resolveCategoryId(String rawName, Map<String, Long> cache, List<String> notes) {
        String name = trimOrNull(rawName);
        if (name == null) {
            return null; // ProductRequestDTO does not require a category — same as the single-create endpoint
        }
        String key = name.toLowerCase(Locale.ROOT);
        Long cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        Long id = categoryRepository.findByCategoryNameIgnoreCase(name)
                .map(Category::getCategoryId)
                .orElse(null);

        if (id == null) {
            CategoryRequest request = CategoryRequest.builder()
                    .categoryName(name)
                    .status("active")
                    .build();
            CategoryResponse created = categoryService.createCategory(request);
            id = created.getCategoryId();
            notes.add("new category \"" + name + "\" created");
        }

        cache.put(key, id);
        return id;
    }

    private String resolveUnit(String rawUnit, Map<String, String> cache, List<String> notes) {
        String name = trimOrNull(rawUnit);
        if (name == null) {
            return null; // ProductRequestDTO does not require a unit either
        }
        String key = name.toLowerCase(Locale.ROOT);
        String cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        String code = unitRepository.findByNameIgnoreCase(name)
                .map(u -> (u.getShortName() != null && !u.getShortName().isBlank()) ? u.getShortName() : u.getUnitName())
                .orElse(null);

        if (code == null) {
            UnitRequestDto dto = new UnitRequestDto();
            dto.setUnitName(name);
            dto.setShortName(name); // a single spreadsheet column: reuse it for both, like the "+ New" unit modal would need both filled
            UnitResponseDto created = unitService.createUnit(dto);
            code = created.getShortName();
            notes.add("new unit \"" + name + "\" created");
        }

        cache.put(key, code);
        return code;
    }

    // =======================
    // FILE-INTERNAL SKU / BARCODE DEDUPE  (spec 3c)
    // =======================
    private String checkFileDuplicates(ProductRequestDTO dto, int rowNumber,
                                        Map<String, Integer> seenSkus, Map<String, Integer> seenBarcodes) {
        if (dto.getSku() != null) {
            String key = dto.getSku().toLowerCase(Locale.ROOT);
            Integer firstRow = seenSkus.get(key);
            if (firstRow != null) {
                return "Duplicate SKU \"" + dto.getSku() + "\" — already used in row " + firstRow + " of this file";
            }
            seenSkus.put(key, rowNumber);
        }
        if (dto.getBarcode() != null) {
            String key = dto.getBarcode().toLowerCase(Locale.ROOT);
            Integer firstRow = seenBarcodes.get(key);
            if (firstRow != null) {
                return "Duplicate barcode \"" + dto.getBarcode() + "\" — already used in row " + firstRow + " of this file";
            }
            seenBarcodes.put(key, rowNumber);
        }
        return null;
    }

    // =======================
    // PARSING
    // =======================
    private record RawRow(int rowNumber, Map<String, String> values) {
    }

    private List<RawRow> parseCsv(InputStream inputStream) throws IOException {
        List<RawRow> rows = new ArrayList<>();
        try (CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .setTrim(true)
                .setIgnoreEmptyLines(true)
                .build()
                .parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            Map<String, String> headerLookup = buildHeaderLookup(parser.getHeaderNames());
            requireHeaders(headerLookup);

            int rowNumber = 1; // header is row 1
            for (CSVRecord record : parser) {
                rowNumber++;
                Map<String, String> values = new LinkedHashMap<>();
                for (String canonical : TEMPLATE_HEADERS) {
                    String actualHeader = headerLookup.get(canonical.toLowerCase(Locale.ROOT));
                    String value = (actualHeader != null && record.isMapped(actualHeader)) ? record.get(actualHeader) : null;
                    values.put(canonical, value);
                }
                rows.add(new RawRow(rowNumber, values));
            }
        }
        return rows;
    }

    private List<RawRow> parseXlsx(InputStream inputStream) throws IOException {
        List<RawRow> rows = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        try (Workbook wb = WorkbookFactory.create(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("Invalid file: no header row found");
            }

            Map<String, Integer> headerIndex = new LinkedHashMap<>();
            for (Cell cell : headerRow) {
                String name = formatter.formatCellValue(cell).trim();
                if (!name.isEmpty()) {
                    headerIndex.put(name.toLowerCase(Locale.ROOT), cell.getColumnIndex());
                }
            }
            requireHeaders(headerIndex);

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                Map<String, String> values = new LinkedHashMap<>();
                for (String canonical : TEMPLATE_HEADERS) {
                    Integer idx = headerIndex.get(canonical.toLowerCase(Locale.ROOT));
                    String value = idx == null ? null : formatter.formatCellValue(row.getCell(idx)).trim();
                    values.put(canonical, value);
                }
                rows.add(new RawRow(r + 1, values)); // POI rows are 0-based; +1 so header = row 1
            }
        }
        return rows;
    }

    private Map<String, String> buildHeaderLookup(List<String> actualHeaders) {
        Map<String, String> lookup = new LinkedHashMap<>();
        for (String h : actualHeaders) {
            lookup.put(h.trim().toLowerCase(Locale.ROOT), h);
        }
        return lookup;
    }

    private void requireHeaders(Map<String, ?> headerLookup) {
        List<String> missing = new ArrayList<>();
        for (String canonical : TEMPLATE_HEADERS) {
            if (!headerLookup.containsKey(canonical.toLowerCase(Locale.ROOT))) {
                missing.add(canonical);
            }
        }
        if (!missing.isEmpty()) {
            throw new RuntimeException("Missing required column(s): " + String.join(", ", missing)
                    + ". Use the downloadable template.");
        }
    }

    // =======================
    // SMALL HELPERS
    // =======================
    private boolean isBlankRow(Map<String, String> values) {
        return values.values().stream().allMatch(v -> v == null || v.isBlank());
    }

    private String trimOrNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private BigDecimal parseDecimal(String raw, String field, List<String> errors) {
        String v = trimOrNull(raw);
        if (v == null) {
            return null;
        }
        try {
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            errors.add(field + " must be a number (got \"" + v + "\")");
            return null;
        }
    }

    private Integer parseInt(String raw, String field, List<String> errors) {
        String v = trimOrNull(raw);
        if (v == null) {
            return null;
        }
        try {
            return (int) Double.parseDouble(v); // tolerates "10.0" from spreadsheet numeric cells
        } catch (NumberFormatException e) {
            errors.add(field + " must be a whole number (got \"" + v + "\")");
            return null;
        }
    }

    private Double parseDouble(String raw, String field, List<String> errors) {
        String v = trimOrNull(raw);
        if (v == null) {
            return null;
        }
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            errors.add(field + " must be a number (got \"" + v + "\")");
            return null;
        }
    }

    private ProductImportRowResultDTO failure(int rowNumber, String productName, String message) {
        return ProductImportRowResultDTO.builder()
                .rowNumber(rowNumber)
                .productName(productName)
                .status("failed")
                .message(message)
                .build();
    }
}
