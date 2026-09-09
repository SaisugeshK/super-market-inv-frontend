package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.model.DocumentCounter;
import com.example.InventoryManagementSystem.Repository.DocumentCounterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Concurrency-safe document numbering. A pessimistic row lock on the counter
 * serialises concurrent callers, so numbers are always unique and monotonic and
 * are never reused when a document is deleted.
 *
 * Invoices and POS sales share one counter ("DOC") so the two tables can never
 * mint the same "INV-YYYYMMDD-NNNNN" string.
 */
@Service
public class DocumentNumberService {

    private static final String SHARED_COUNTER = "DOC";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final DocumentCounterRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    public DocumentNumberService(DocumentCounterRepository repository) {
        this.repository = repository;
    }

    /**
     * On boot, seed the shared counter above the highest number already present
     * in either table (covers a migration from the old count()-based scheme).
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initialiseCounter() {
        if (repository.findById(SHARED_COUNTER).isPresent()) {
            return;
        }
        long maxExisting = 0L;
        maxExisting = Math.max(maxExisting, maxSuffix("invoices", "invoice_number"));
        maxExisting = Math.max(maxExisting, maxSuffix("sales", "invoice_number"));
        repository.save(new DocumentCounter(SHARED_COUNTER, maxExisting));
    }

    private long maxSuffix(String table, String column) {
        try {
            Object result = entityManager.createNativeQuery(
                    "SELECT COALESCE(MAX(CAST(SPLIT_PART(" + column + ", '-', 3) AS BIGINT)), 0) "
                            + "FROM " + table + " WHERE " + column + " ~ '^INV-[0-9]{8}-[0-9]+$'")
                    .getSingleResult();
            return result == null ? 0L : ((Number) result).longValue();
        } catch (RuntimeException ex) {
            return 0L;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextInvoiceNumber() {
        DocumentCounter counter = repository.findByCounterName(SHARED_COUNTER)
                .orElseGet(() -> repository.save(new DocumentCounter(SHARED_COUNTER, 0L)));

        long next = counter.getCurrentValue() + 1;
        counter.setCurrentValue(next);
        repository.save(counter);

        return String.format("INV-%s-%05d", LocalDate.now().format(DATE), next);
    }
}
