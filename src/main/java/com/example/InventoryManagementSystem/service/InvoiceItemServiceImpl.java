package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.InvoiceItemDto;
import com.example.InventoryManagementSystem.model.InvoiceItem;
import com.example.InventoryManagementSystem.Repository.InvoiceItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceItemServiceImpl
        implements InvoiceItemService {

    @Autowired
    private InvoiceItemRepository repository;

    @Override
    public InvoiceItemDto createInvoiceItem(
            InvoiceItemDto dto) {

        InvoiceItem item = new InvoiceItem();

        item.setInvoiceId(dto.getInvoiceId());
        item.setProductId(dto.getProductId());
        item.setBarcode(dto.getBarcode());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(dto.getUnitPrice());
        item.setDiscount(dto.getDiscount());
        item.setTaxPercentage(dto.getTaxPercentage());
        item.setTaxAmount(dto.getTaxAmount());
        item.setTotalAmount(dto.getTotalAmount());

        InvoiceItem saved = repository.save(item);

        dto.setInvoiceItemId(saved.getInvoiceItemId());

        return dto;
    }

    @Override
    public List<InvoiceItemDto> getAllInvoiceItems() {

        return repository.findAll().stream().map(item -> {

            InvoiceItemDto dto = new InvoiceItemDto();

            dto.setInvoiceItemId(item.getInvoiceItemId());
            dto.setInvoiceId(item.getInvoiceId());
            dto.setProductId(item.getProductId());
            dto.setBarcode(item.getBarcode());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setDiscount(item.getDiscount());
            dto.setTaxPercentage(item.getTaxPercentage());
            dto.setTaxAmount(item.getTaxAmount());
            dto.setTotalAmount(item.getTotalAmount());

            return dto;

        }).collect(Collectors.toList());
    }

    @Override
    public InvoiceItemDto getInvoiceItemById(Long id) {

        InvoiceItem item = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Invoice Item not found"));

        InvoiceItemDto dto = new InvoiceItemDto();

        dto.setInvoiceItemId(item.getInvoiceItemId());
        dto.setInvoiceId(item.getInvoiceId());
        dto.setProductId(item.getProductId());
        dto.setBarcode(item.getBarcode());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscount(item.getDiscount());
        dto.setTaxPercentage(item.getTaxPercentage());
        dto.setTaxAmount(item.getTaxAmount());
        dto.setTotalAmount(item.getTotalAmount());

        return dto;
    }

    @Override
    public InvoiceItemDto updateInvoiceItem(Long id,
                                            InvoiceItemDto dto) {

        InvoiceItem item = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Invoice Item not found"));

        item.setInvoiceId(dto.getInvoiceId());
        item.setProductId(dto.getProductId());
        item.setBarcode(dto.getBarcode());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(dto.getUnitPrice());
        item.setDiscount(dto.getDiscount());
        item.setTaxPercentage(dto.getTaxPercentage());
        item.setTaxAmount(dto.getTaxAmount());
        item.setTotalAmount(dto.getTotalAmount());

        repository.save(item);

        dto.setInvoiceItemId(item.getInvoiceItemId());

        return dto;
    }

    @Override
    public void deleteInvoiceItem(Long id) {

        repository.deleteById(id);
    }
}