package com.springbyexample.v2.integrationbetweenspringtechnologies.config;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.springbyexample.v2.integrationbetweenspringtechnologies.catalog.ProductNotFoundException;
import com.springbyexample.v2.integrationbetweenspringtechnologies.inventory.InsufficientStockException;
import com.springbyexample.v2.integrationbetweenspringtechnologies.ordering.OrderNotFoundException;

/**
 * Handles application exceptions and translates them into HTTP responses.
 *
 * @author Mujuzi Moses
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({ProductNotFoundException.class, OrderNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientStock(InsufficientStockException exception) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequest(IllegalArgumentException exception) {

        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }
}
