package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.SalesItemRepository;
import com.example.InventoryManagementSystem.Repository.SalesRepository;
import com.example.InventoryManagementSystem.Repository.SalesReturnRepository;
import com.example.InventoryManagementSystem.Repository.StockMovementRepository;
import com.example.InventoryManagementSystem.dto.SalesReturnRequestDTO;
import com.example.InventoryManagementSystem.dto.SalesReturnResponseDTO;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.SalesItem;
import com.example.InventoryManagementSystem.model.SalesReturn;
import com.example.InventoryManagementSystem.model.StockMovement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesReturnServiceImpl implements SalesReturnService {

    private static final String NOT_FOUND = "Sales Return not found";

    private final SalesReturnRepository salesReturnRepository;
    private final SalesItemRepository salesItemRepository;
    private final SalesRepository salesRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    // CREATE — validates against the original sale line, restores stock,
    // computes the refund amount server-side, all in one transaction.
    @Override
    @Transactional
    public SalesReturnResponseDTO createReturn(SalesReturnRequestDTO dto) {

        Integer qty = dto.getReturnQuantity();
        if (qty == null || qty <= 0) {
            throw new RuntimeException("returnQuantity must be positive");
        }

        SalesItem line = null;
        if (dto.getSalesItemId() != null) {
            line = salesItemRepository.findById(dto.getSalesItemId())
                    .orElseThrow(() -> new RuntimeException("Sales item not found"));

            if (dto.getSaleId() != null && line.getSaleId() != null
                    && !dto.getSaleId().equals(line.getSaleId())) {
                throw new RuntimeException("Sales item does not belong to that sale");
            }

            int alreadyReturned = salesReturnRepository.sumReturnedQtyForItem(line.getSaleItemId());
            int sold = line.getQuantity() != null ? line.getQuantity() : 0;
            if (alreadyReturned + qty > sold) {
                throw new RuntimeException("Return quantity exceeds the quantity sold ("
                        + sold + " sold, " + alreadyReturned + " already returned)");
            }
        }

        // Refund amount: from the sold line price when we have it, else the client value.
        BigDecimal totalAmount;
        if (line != null && line.getSellingPrice() != null) {
            totalAmount = line.getSellingPrice()
                    .multiply(BigDecimal.valueOf(qty))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            totalAmount = dto.getTotalAmount() != null ? dto.getTotalAmount() : BigDecimal.ZERO;
        }

        SalesReturn salesReturn = SalesReturn.builder()
                .salesItemId(dto.getSalesItemId())
                .saleId(dto.getSaleId())
                .customerId(dto.getCustomerId())
                .returnQuantity(qty)
                .reason(dto.getReason())
                .notes(dto.getNotes())
                .totalAmount(totalAmount)
                .refundStatus(dto.getRefundStatus() != null ? dto.getRefundStatus() : "PENDING")
                .returnDate(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .build();

        SalesReturn saved = salesReturnRepository.save(salesReturn);

        // Returned goods go back on the shelf.
        if (line != null && line.getProductId() != null) {
            restock(line.getProductId(), qty, saved.getReturnId(), "SALES_RETURN_IN");
        }

        return mapToDTO(saved);
    }

    @Override
    public SalesReturnResponseDTO getById(Long id) {
        return mapToDTO(salesReturnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(NOT_FOUND)));
    }

    @Override
    public List<SalesReturnResponseDTO> getAll() {
        return salesReturnRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // UPDATE — metadata edit (reason / notes / refund status). Quantity and the
    // stock effect are fixed at creation; to change them, delete and re-create.
    @Override
    @Transactional
    public SalesReturnResponseDTO updateReturn(Long id, SalesReturnRequestDTO dto) {

        SalesReturn salesReturn = salesReturnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(NOT_FOUND));

        salesReturn.setReason(dto.getReason());
        salesReturn.setNotes(dto.getNotes());
        if (dto.getRefundStatus() != null) {
            salesReturn.setRefundStatus(dto.getRefundStatus());
        }
        if (dto.getCustomerId() != null) {
            salesReturn.setCustomerId(dto.getCustomerId());
        }

        return mapToDTO(salesReturnRepository.save(salesReturn));
    }

    // DELETE — reverse the stock that was put back when the return was created.
    @Override
    @Transactional
    public void delete(Long id) {

        SalesReturn salesReturn = salesReturnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(NOT_FOUND));

        Integer qty = salesReturn.getReturnQuantity();
        if (salesReturn.getSalesItemId() != null && qty != null && qty > 0) {
            salesItemRepository.findById(salesReturn.getSalesItemId())
                    .map(SalesItem::getProductId)
                    .ifPresent(productId ->
                            restock(productId, -qty, salesReturn.getReturnId(), "SALES_RETURN_REVERSAL"));
        }

        salesReturnRepository.delete(salesReturn);
    }

    private void restock(Long productId, int delta, Long referenceId, String movementType) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElse(null);
        if (product == null) return;

        int current = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        int updated = current + delta;
        if (updated < 0) {
            throw new RuntimeException("Reversing this return would make stock negative");
        }
        product.setStockQuantity(updated);
        productRepository.save(product);

        stockMovementRepository.save(StockMovement.builder()
                .product(product)
                .movementType(movementType)
                .quantity(Math.abs(delta))
                .referenceId(referenceId)
                .notes(movementType.replace('_', ' ').toLowerCase())
                .build());
    }

    private SalesReturnResponseDTO mapToDTO(SalesReturn salesReturn) {
        return SalesReturnResponseDTO.builder()
                .returnId(salesReturn.getReturnId())
                .salesItemId(salesReturn.getSalesItemId())
                .saleId(salesReturn.getSaleId())
                .customerId(salesReturn.getCustomerId())
                .returnQuantity(salesReturn.getReturnQuantity())
                .reason(salesReturn.getReason())
                .notes(salesReturn.getNotes())
                .totalAmount(salesReturn.getTotalAmount())
                .refundStatus(salesReturn.getRefundStatus())
                .returnDate(salesReturn.getReturnDate())
                .createdAt(salesReturn.getCreatedAt())
                .build();
    }
}
