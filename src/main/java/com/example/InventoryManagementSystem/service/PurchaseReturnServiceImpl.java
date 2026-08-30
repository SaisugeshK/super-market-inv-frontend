package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.PurchaseRepository;
import com.example.InventoryManagementSystem.Repository.SupplierRepository;
import com.example.InventoryManagementSystem.dto.PurchaseReturnRequestDTO;
import com.example.InventoryManagementSystem.dto.PurchaseReturnResponseDTO;
import com.example.InventoryManagementSystem.model.PurchaseReturn;
import com.example.InventoryManagementSystem.Repository.PurchaseReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseReturnServiceImpl implements PurchaseReturnService {

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public PurchaseReturnResponseDTO createPurchaseReturn(PurchaseReturnRequestDTO requestDTO) {

        // validate purchase exists
        purchaseRepository.findById(requestDTO.getPurchaseId())
                .orElseThrow(() -> new RuntimeException("Purchase not found"));

        // validate supplier exists
        supplierRepository.findById(requestDTO.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        PurchaseReturn entity = PurchaseReturn.builder()
                .purchaseId(requestDTO.getPurchaseId().intValue())
                .supplierId(requestDTO.getSupplierId().intValue())
                .returnDate(LocalDateTime.now())
                .totalAmount(requestDTO.getTotalAmount())
                .notes(requestDTO.getNotes())
                .build();

        PurchaseReturn saved = purchaseReturnRepository.save(entity);

        return mapToResponse(saved);
    }

    @Override
    public PurchaseReturnResponseDTO getPurchaseReturnById(Integer id) {

        PurchaseReturn entity = purchaseReturnRepository.findById(id).orElse(null);

        if (entity == null) {
            return null;
        }

        return mapToResponse(entity);
    }

    @Override
    public List<PurchaseReturnResponseDTO> getAllPurchaseReturns() {

        return purchaseReturnRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PurchaseReturnResponseDTO updatePurchaseReturn(Integer id, PurchaseReturnRequestDTO requestDTO) {

        PurchaseReturn entity = purchaseReturnRepository.findById(id).orElse(null);

        if (entity == null) {
            return null;
        }

        entity.setPurchaseId(requestDTO.getPurchaseId().intValue());
        entity.setSupplierId(requestDTO.getSupplierId().intValue());
        entity.setTotalAmount(requestDTO.getTotalAmount());
        entity.setNotes(requestDTO.getNotes());

        PurchaseReturn updated = purchaseReturnRepository.save(entity);

        return mapToResponse(updated);
    }

    @Override
    public void deletePurchaseReturn(Integer id) {

        PurchaseReturn entity = purchaseReturnRepository.findById(id).orElse(null);

        if (entity != null) {
            purchaseReturnRepository.delete(entity);
        }
    }

    private PurchaseReturnResponseDTO mapToResponse(PurchaseReturn entity) {

        return PurchaseReturnResponseDTO.builder()
                .purchaseReturnId(entity.getPurchaseReturnId())
                .purchaseId(entity.getPurchaseId())
                .supplierId(entity.getSupplierId())
                .returnDate(entity.getReturnDate())
                .totalAmount(entity.getTotalAmount())
                .notes(entity.getNotes())
                .build();
    }
}