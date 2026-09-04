package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "billing_counters")
public class BillingCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "counter_id")
    private Long counterId;

    @Column(name = "counter_name", length = 50)
    private String counterName;

    @Column(length = 100)
    private String location;

    @Column(length = 20)
    private String status = "active";

    // Fallback opening CASH balance used only when the counter has never been closed before
    @Column(name = "initial_opening_cash", precision = 12, scale = 2)
    private BigDecimal initialOpeningCash;

    public BigDecimal getInitialOpeningCash() {
        return initialOpeningCash;
    }

    public void setInitialOpeningCash(BigDecimal initialOpeningCash) {
        this.initialOpeningCash = initialOpeningCash;
    }

    // Getters and Setters

    public Long getCounterId() {
        return counterId;
    }

    public void setCounterId(Long counterId) {
        this.counterId = counterId;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}