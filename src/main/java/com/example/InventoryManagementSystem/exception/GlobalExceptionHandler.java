package com.example.InventoryManagementSystem.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String GENERIC_SERVER_ERROR = "Unexpected server error";

    private ResponseEntity<Object> body(HttpStatus status, String message, Map<String, Object> extra) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("timestamp", LocalDateTime.now());
        b.put("status", status.value());
        b.put("message", message);
        if (extra != null) b.putAll(extra);
        return ResponseEntity.status(status).body(b);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // Unmatched URL / missing static resource
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResource(NoResourceFoundException ex) {
        return body(HttpStatus.NOT_FOUND, "Requested resource was not found", null);
    }

    // Explicit status thrown by services (e.g. auth -> 401)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String reason = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return body(status, reason, null);
    }

    // Bean-validation failures (@Valid on @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fields.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return body(HttpStatus.BAD_REQUEST, "Validation failed", Map.of("errors", fields));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraint(ConstraintViolationException ex) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleUnreadable(HttpMessageNotReadableException ex) {
        return body(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body", null);
    }

    // FK / unique constraint violations — 409 instead of a raw 500
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex) {
        return body(HttpStatus.CONFLICT,
                "Record is referenced by other data, or a duplicate value was supplied", null);
    }

    // The codebase's not-found / business-rule idiom is `throw new RuntimeException("...")`.
    // Map known message shapes to real status codes; never leak a stack trace.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            return body(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_SERVER_ERROR, null);
        }
        String m = msg.toLowerCase();
        if (m.contains("not found")) {
            return body(HttpStatus.NOT_FOUND, msg, null);
        }
        if (m.contains("already exists") || m.contains("in use") || m.contains("duplicate")) {
            return body(HttpStatus.CONFLICT, msg, null);
        }
        if (m.contains("not enough stock") || m.contains("cannot be negative")
                || m.contains("must be positive") || m.contains("invalid")
                || m.contains("not set") || m.contains("required")
                || m.contains("exceeds") || m.contains("out of stock")) {
            return body(HttpStatus.BAD_REQUEST, msg, null);
        }
        // Unknown runtime failure: report as a server error, but sanitised.
        return body(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_SERVER_ERROR, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAny(Exception ex) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_SERVER_ERROR, null);
    }
}
