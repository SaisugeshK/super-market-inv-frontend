package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class BillingCounterDto {

    private Long counterId;

    @NotBlank(message = "counterName is required")
    private String counterName;
    private String location;
    private String status;
    private BigDecimal initialOpeningCash;

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

    public BigDecimal getInitialOpeningCash() {
        return initialOpeningCash;
    }

    public void setInitialOpeningCash(BigDecimal initialOpeningCash) {
        this.initialOpeningCash = initialOpeningCash;
    }
}