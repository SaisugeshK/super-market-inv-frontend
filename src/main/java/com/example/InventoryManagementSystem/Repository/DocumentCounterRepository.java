package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.DocumentCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface DocumentCounterRepository extends JpaRepository<DocumentCounter, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DocumentCounter> findByCounterName(String counterName);
}
