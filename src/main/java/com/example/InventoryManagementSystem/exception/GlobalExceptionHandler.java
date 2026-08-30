package com.example.InventoryManagementSystem.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "message", ex.getMessage(),
                        "status", 404
                ));
    }

    // FK / unique constraint violations — return 409 instead of a raw 500
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "message", "Record is referenced by other data and cannot be deleted or changed",
                        "status", 409
                ));
    }
}