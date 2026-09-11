package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.SalesRequestDTO;
import com.example.InventoryManagementSystem.dto.SalesResponseDTO;
import com.example.InventoryManagementSystem.model.Sales;
import com.example.InventoryManagementSystem.Repository.SalesItemRepository;
import com.example.InventoryManagementSystem.Repository.SalesRepository;
import com.example.InventoryManagementSystem.Repository.StockMovementRepository;
import com.example.InventoryManagementSystem.Repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesServiceImpl implements SalesService {

    private final SalesRepository salesRepository;
    private final SalesItemRepository salesItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final DocumentNumberService documentNumberService;

    // AUTO-GENERATE invoice number: INV-YYYYMMDD-XXXXX (concurrency-safe)
    private String generateInvoiceNumber() {
        return documentNumberService.nextInvoiceNumber();
    }

    // CREATE SALE
    @Override
    public SalesResponseDTO createSale(SalesRequestDTO dto) {

        Sales sale = new Sales();

        String invoiceNumber = (dto.getInvoiceNumber() != null && !dto.getInvoiceNumber().isBlank())
                ? dto.getInvoiceNumber()
                : generateInvoiceNumber();

        sale.setCustomerId(dto.getCustomerId());
        sale.setCreatedBy(dto.getCreatedBy());
        sale.setCounterId(dto.getCounterId());
        sale.setInvoiceNumber(invoiceNumber);
        sale.setPaymentMethod(dto.getPaymentMethod());
        sale.setPaymentStatus(dto.getPaymentStatus());
        sale.setTotalAmount(dto.getTotalAmount());
        sale.setSaleDate(LocalDateTime.now());

        Sales saved = salesRepository.save(sale);

        return mapToDTO(saved);
    }

    // GET BY ID
    @Override
    public SalesResponseDTO getSaleById(Long id) {

        Sales sale = salesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));

        return mapToDTO(sale);
    }

    // GET ALL
    @Override
    public List<SalesResponseDTO> getAllSales() {

        return salesRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Override
    public SalesResponseDTO updateSale(Long id, SalesRequestDTO dto) {

        Sales sale = salesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));

        sale.setCustomerId(dto.getCustomerId());
        sale.setCreatedBy(dto.getCreatedBy());
        sale.setCounterId(dto.getCounterId());
        sale.setInvoiceNumber(dto.getInvoiceNumber());
        sale.setPaymentMethod(dto.getPaymentMethod());
        sale.setPaymentStatus(dto.getPaymentStatus());
        sale.setTotalAmount(dto.getTotalAmount());

        Sales updated = salesRepository.save(sale);

        return mapToDTO(updated);
    }

    // DELETE
    @Override
    @Transactional
    public void deleteSale(Long id) {

        Sales sale = salesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));

        // SalesItem, StockMovement and PaymentTransaction all carry a plain id
        // (not a mapped relation) so JPA won't cascade — remove them
        // explicitly or they become orphans. (Stock is intentionally left
        // as-is: deleting the sale record does not imply the goods came back
        // onto the shelf — use a sales return for that.)
        salesItemRepository.deleteAll(salesItemRepository.findBySaleId(id));
        stockMovementRepository.deleteAll(
                stockMovementRepository.findByMovementTypeAndReferenceId("SALE_OUT", id));
        paymentTransactionRepository.deleteAll(paymentTransactionRepository.findByInvoiceId(id));
        salesRepository.delete(sale);
    }

    // MAPPER METHOD
    private SalesResponseDTO mapToDTO(Sales sale) {

        SalesResponseDTO dto = new SalesResponseDTO();

        dto.setSaleId(sale.getSaleId());
        dto.setCustomerId(sale.getCustomerId());
        dto.setCreatedBy(sale.getCreatedBy());
        dto.setCounterId(sale.getCounterId());
        dto.setInvoiceNumber(sale.getInvoiceNumber());
        dto.setPaymentMethod(sale.getPaymentMethod());
        dto.setPaymentStatus(sale.getPaymentStatus());
        dto.setTotalAmount(sale.getTotalAmount());
        dto.setSubtotal(sale.getSubtotal());
        dto.setDiscountAmount(sale.getDiscountAmount());
        dto.setTaxAmount(sale.getTaxAmount());
        dto.setPaidAmount(sale.getPaidAmount());
        dto.setBalanceAmount(sale.getBalanceAmount());
        dto.setSaleDate(sale.getSaleDate());

        return dto;
    }
}