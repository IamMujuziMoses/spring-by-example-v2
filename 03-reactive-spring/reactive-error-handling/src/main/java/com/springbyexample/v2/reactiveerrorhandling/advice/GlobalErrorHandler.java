package com.springbyexample.v2.reactiveerrorhandling.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.springbyexample.v2.reactiveerrorhandling.exception.ApiError;
import com.springbyexample.v2.reactiveerrorhandling.exception.InvalidProductIdException;
import com.springbyexample.v2.reactiveerrorhandling.exception.ProductNotFoundException;
import com.springbyexample.v2.reactiveerrorhandling.exception.ServiceOperationException;

import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@RestControllerAdvice
public class GlobalErrorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @ExceptionHandler(InvalidProductIdException.class)
    public Mono<ResponseEntity<ApiError>> handleInvalidProductId(InvalidProductIdException exception) {

        return Mono.just(ResponseEntity.badRequest()
                .body(new ApiError("INVALID_PRODUCT_ID", exception.getMessage())));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleProductNotFound(ProductNotFoundException exception) {

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("PRODUCT_NOT_FOUND", exception.getMessage())));
    }

    @ExceptionHandler(ServiceOperationException.class)
    public Mono<ResponseEntity<ApiError>> handleServiceOperation(ServiceOperationException exception) {

        LOG.error("Service operation failed", exception);

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("SERVICE_OPERATION_FAILED", exception.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleUnexpectedError(Exception exception) {

        LOG.error("Unexpected application error", exception);

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("INTERNAL_SERVER_ERROR", "An unexpected error occurred")));
    }
}
