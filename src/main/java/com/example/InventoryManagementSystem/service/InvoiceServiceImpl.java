package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.InvoiceDto;
import com.example.InventoryManagementSystem.model.Invoice;
import com.example.InventoryManagementSystem.Repository.InvoiceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository repository;

    // AUTO-GENERATE invoice number: INV-YYYYMMDD-XXXXX
    private String generateInvoiceNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = repository.count() + 1;
        return String.format("INV-%s-%05d", datePart, count);
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
        invoice.setSubtotal(dto.getSubtotal());
        invoice.setDiscountAmount(dto.getDiscountAmount());
        invoice.setTaxAmount(dto.getTaxAmount());
        invoice.setGrandTotal(dto.getGrandTotal());
        invoice.setPaidAmount(dto.getPaidAmount());
        invoice.setBalanceAmount(dto.getBalanceAmount());
        invoice.setPaymentMethod(dto.getPaymentMethod());
        invoice.setPaymentStatus(dto.getPaymentStatus());
        invoice.setCreatedBy(dto.getCreatedBy());

        Invoice saved = repository.save(invoice);

        dto.setInvoiceId(saved.getInvoiceId());
        dto.setInvoiceNumber(saved.getInvoiceNumber());

        return dto;
    }

    @Override
    public List<InvoiceDto> getAllInvoices() {

        return repository.findAll().stream().map(invoice -> {

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

        }).collect(Collectors.toList());
    }

    @Override
    public InvoiceDto getInvoiceById(Long id) {

        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

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
    public InvoiceDto updateInvoice(Long id, InvoiceDto dto) {

        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setCustomerId(dto.getCustomerId());
        invoice.setCounterId(dto.getCounterId());
        invoice.setSubtotal(dto.getSubtotal());
        invoice.setDiscountAmount(dto.getDiscountAmount());
        invoice.setTaxAmount(dto.getTaxAmount());
        invoice.setGrandTotal(dto.getGrandTotal());
        invoice.setPaidAmount(dto.getPaidAmount());
        invoice.setBalanceAmount(dto.getBalanceAmount());
        invoice.setPaymentMethod(dto.getPaymentMethod());
        invoice.setPaymentStatus(dto.getPaymentStatus());
        invoice.setCreatedBy(dto.getCreatedBy());

        repository.save(invoice);

        dto.setInvoiceId(invoice.getInvoiceId());

        return dto;
    }

    @Override
    public void deleteInvoice(Long id) {
        repository.deleteById(id);
    }
}