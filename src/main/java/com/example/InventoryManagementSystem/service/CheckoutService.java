package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.CheckoutRequestDto;
import com.example.InventoryManagementSystem.dto.CheckoutResponseDto;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.Sales;
import com.example.InventoryManagementSystem.model.SalesItem;
import com.example.InventoryManagementSystem.model.StockMovement;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.ProductTaxRepository;
import com.example.InventoryManagementSystem.Repository.SalesItemRepository;
import com.example.InventoryManagementSystem.Repository.SalesRepository;
import com.example.InventoryManagementSystem.Repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Single-transaction POS checkout. Locks every product row it touches, computes
 * all money on the server from product prices + configured taxes, and writes the
 * sale, its line items, the stock decrements and the stock-movement history
 * atomically — so a mid-way failure rolls the whole thing back.
 *
 * Closes: client-controlled totals, stock races, non-atomic N+1 checkout.
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final ProductRepository productRepository;
    private final ProductTaxRepository productTaxRepository;
    private final SalesRepository salesRepository;
    private final SalesItemRepository salesItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final DocumentNumberService documentNumberService;

    private static BigDecimal money(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public CheckoutResponseDto checkout(CheckoutRequestDto request) {

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;

        List<SalesItem> itemsToSave = new ArrayList<>();
        List<CheckoutResponseDto.Line> responseLines = new ArrayList<>();
        List<Product> productsToSave = new ArrayList<>();
        List<StockMovement> movements = new ArrayList<>();

        for (CheckoutRequestDto.Item line : request.getItems()) {

            Product product = productRepository.findByIdForUpdate(line.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found: " + line.getProductId()));

            if (product.getSellingPrice() == null) {
                throw new RuntimeException("Selling price not set for product " + product.getProductName());
            }

            int qty = line.getQuantity();
            int available = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            if (available < qty) {
                throw new RuntimeException("Not enough stock for " + product.getProductName()
                        + " (have " + available + ", need " + qty + ")");
            }

            BigDecimal unitPrice = money(product.getSellingPrice());
            BigDecimal lineSubtotal = money(unitPrice.multiply(BigDecimal.valueOf(qty)));

            Double pct = productTaxRepository.findTopByProductId(product.getProductId())
                    .map(t -> t.getTaxPercentage() == null ? 0d : t.getTaxPercentage())
                    .orElse(0d);
            BigDecimal lineTax = money(lineSubtotal
                    .multiply(BigDecimal.valueOf(pct))
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));

            subtotal = subtotal.add(lineSubtotal);
            taxTotal = taxTotal.add(lineTax);

            product.setStockQuantity(available - qty);
            productsToSave.add(product);

            SalesItem si = new SalesItem();
            si.setProductId(product.getProductId());
            si.setQuantity(qty);
            si.setSellingPrice(unitPrice);
            si.setTotal(lineSubtotal); // revenue basis, tax tracked at sale level
            itemsToSave.add(si);

            movements.add(StockMovement.builder()
                    .product(product)
                    .movementType("SALE_OUT")
                    .quantity(qty)
                    .notes("POS checkout")
                    .build());

            responseLines.add(CheckoutResponseDto.Line.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .quantity(qty)
                    .unitPrice(unitPrice)
                    .taxPercentage(pct)
                    .lineSubtotal(lineSubtotal)
                    .lineTax(lineTax)
                    .lineTotal(money(lineSubtotal.add(lineTax)))
                    .build());
        }

        subtotal = money(subtotal);
        taxTotal = money(taxTotal);

        BigDecimal discount = money(request.getDiscountAmount());
        BigDecimal maxDiscount = subtotal.add(taxTotal);
        if (discount.compareTo(maxDiscount) > 0) {
            discount = maxDiscount; // never let a discount push the bill negative
        }

        BigDecimal grandTotal = money(subtotal.add(taxTotal).subtract(discount));
        BigDecimal paid = money(request.getPaidAmount());
        BigDecimal balance = money(grandTotal.subtract(paid));

        String status;
        if (paid.compareTo(grandTotal) >= 0) {
            status = "PAID";
        } else if (paid.signum() > 0) {
            status = "PARTIAL";
        } else {
            status = "PENDING";
        }

        Sales sale = new Sales();
        sale.setCustomerId(request.getCustomerId());
        sale.setCounterId(request.getCounterId());
        sale.setCreatedBy(request.getCreatedBy());
        sale.setInvoiceNumber(documentNumberService.nextInvoiceNumber());
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setPaymentStatus(status);
        sale.setSubtotal(subtotal);
        sale.setDiscountAmount(discount);
        sale.setTaxAmount(taxTotal);
        sale.setTotalAmount(grandTotal);
        sale.setPaidAmount(paid);
        sale.setBalanceAmount(balance.signum() < 0 ? BigDecimal.ZERO.setScale(2) : balance);
        sale.setSaleDate(LocalDateTime.now());
        Sales savedSale = salesRepository.save(sale);

        productRepository.saveAll(productsToSave);

        for (SalesItem si : itemsToSave) {
            si.setSaleId(savedSale.getSaleId());
        }
        salesItemRepository.saveAll(itemsToSave);

        Long ref = savedSale.getSaleId();
        for (StockMovement m : movements) {
            m.setReferenceId(ref);
        }
        stockMovementRepository.saveAll(movements);

        return CheckoutResponseDto.builder()
                .saleId(savedSale.getSaleId())
                .invoiceNumber(savedSale.getInvoiceNumber())
                .customerId(savedSale.getCustomerId())
                .counterId(savedSale.getCounterId())
                .paymentMethod(savedSale.getPaymentMethod())
                .paymentStatus(status)
                .subtotal(subtotal)
                .discountAmount(discount)
                .taxAmount(taxTotal)
                .grandTotal(grandTotal)
                .paidAmount(paid)
                .balanceAmount(sale.getBalanceAmount())
                .saleDate(savedSale.getSaleDate())
                .items(responseLines)
                .build();
    }
}
