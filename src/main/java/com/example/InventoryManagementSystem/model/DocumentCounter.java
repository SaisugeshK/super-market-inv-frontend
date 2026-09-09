package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;

/**
 * A single incrementing counter row per document family, updated under a
 * pessimistic row lock so concurrent invoice/sale creation never collides.
 */
@Entity
@Table(name = "document_counters")
public class DocumentCounter {

    @Id
    @Column(name = "counter_name", length = 40)
    private String counterName;

    @Column(name = "current_value", nullable = false)
    private long currentValue;

    public DocumentCounter() {
    }

    public DocumentCounter(String counterName, long currentValue) {
        this.counterName = counterName;
        this.currentValue = currentValue;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }

    public long getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(long currentValue) {
        this.currentValue = currentValue;
    }
}
