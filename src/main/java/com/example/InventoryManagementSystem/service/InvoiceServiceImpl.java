package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.InvoiceDto;
import com.example.InventoryManagementSystem.model.Invoice;
import com.example.InventoryManagementSystem.Repository.InvoiceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository repository;

    @Autowired
    private DocumentNumberService documentNumberService;

    private static BigDecimal money(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }

    // Server-side money: never trust the client's grand total / balance.
    private void applyComputedTotals(Invoice invoice, InvoiceDto dto) {
        BigDecimal subtotal = money(dto.getSubtotal());
        BigDecimal discount = money(dto.getDiscountAmount());
        BigDecimal tax = money(dto.getTaxAmount());
        BigDecimal grand = money(subtotal.subtract(discount).add(tax));
        if (grand.signum() < 0) grand = BigDecimal.ZERO.setScale(2);
        BigDecimal paid = money(dto.getPaidAmount());
        BigDecimal balance = money(grand.subtract(paid));
        if (balance.signum() < 0) balance = BigDecimal.ZERO.setScale(2);

        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(discount);
        invoice.setTaxAmount(tax);
        invoice.setGrandTotal(grand);
        invoice.setPaidAmount(paid);
        invoice.setBalanceAmount(balance);

        String status;
        if (paid.compareTo(grand) >= 0 && grand.signum() > 0) {
            status = "PAID";
        } else if (paid.signum() > 0) {
            status = "PARTIAL";
        } else {
            status = "PENDING";
        }
        invoice.setPaymentStatus(status);
    }

    // AUTO-GENERATE invoice number: INV-YYYYMMDD-XXXXX (concurrency-safe)
    private String generateInvoiceNumber() {
        return documentNumberService.nextInvoiceNumber();
    }

    @Override
    public InvoiceDto createInvoice(InvoiceDto dto) {

        Invoice invoice = new Invoice();

        // auto-generate if not provided
        String invoiceNumber = (dto.getInvoiceNumber() != null && !dto.getInvoiceNumber().isBlank())
                ? dto.getInvoiceNumber()
                : generateInvoiceNumber();

        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomerId(dto.getCustomerId());
        invoice.setCounterId(dto.getCounterId());
        applyComputedTotals(invoice, dto);
        invoice.setPaymentMethod(dto.getPaymentMethod());
        invoice.setCreatedBy(dto.getCreatedBy());

        Invoice saved = repository.save(invoice);

        return toDto(saved);
    }

    private InvoiceDto toDto(Invoice invoice) {
        InvoiceDto dto = new InvoiceDto();
        dto.setInvoiceId(invoice.getInvoiceId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setCustomerId(invoice.getCustomerId());
        dto.setCounterId(invoice.getCounterId());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setDiscountAmount(invoice.getDiscountAmount());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setGrandTotal(invoice.getGrandTotal());
        dto.setPaidAmount(invoice.getPaidAmount());
        dto.setBalanceAmount(invoice.getBalanceAmount());
        dto.setPaymentMethod(invoice.getPaymentMethod());
        dto.setPaymentStatus(invoice.getPaymentStatus());
        dto.setCreatedBy(invoice.getCreatedBy());
        return dto;
    }

    @Override
    public List<InvoiceDto> getAllInvoices() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public InvoiceDto getInvoiceById(Long id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return toDto(invoice);
    }

    @Override
    public InvoiceDto updateInvoice(Long id, InvoiceDto dto) {

        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (dto.getInvoiceNumber() != null && !dto.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber(dto.getInvoiceNumber());
        }
        invoice.setCustomerId(dto.getCustomerId());
        invoice.setCounterId(dto.getCounterId());
        applyComputedTotals(invoice, dto);
        invoice.setPaymentMethod(dto.getPaymentMethod());
        invoice.setCreatedBy(dto.getCreatedBy());

        return toDto(repository.save(invoice));
    }

    @Override
    public void deleteInvoice(Long id) {
        repository.deleteById(id);
    }
}