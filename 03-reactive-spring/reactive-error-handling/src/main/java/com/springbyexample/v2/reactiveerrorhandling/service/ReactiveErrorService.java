package com.springbyexample.v2.reactiveerrorhandling.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.springbyexample.v2.reactiveerrorhandling.exception.InvalidProductIdException;
import com.springbyexample.v2.reactiveerrorhandling.exception.ProductNotFoundException;
import com.springbyexample.v2.reactiveerrorhandling.exception.ServiceOperationException;
import com.springbyexample.v2.reactiveerrorhandling.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@Service
public class ReactiveErrorService {

    private static final Logger LOG = LoggerFactory.getLogger(ReactiveErrorService.class);

    private final Map<Long, Product> products = Map.of(
            1L, new Product(1L, "Laptop"),
            2L, new Product(2L, "Keyboard"),
            3L, new Product(3L, "Monitor")
    );

    public Flux<Product> findAll() {
        return Flux.fromIterable(products.values());
    }

    public Mono<Product> findById(Long id) {
        return Mono.defer(() -> {
            if (id == null || id <= 0) {
                return Mono.error(new InvalidProductIdException(id));
            }

            Product product = products.get(id);

            if (product == null) {
                return Mono.error(new ProductNotFoundException(id));
            }

            return Mono.just(product);
        });
    }

    public Mono<String> demonstrateOnErrorReturn() {
        return Mono.<String>error(new IllegalStateException("Primary operation failed"))
                .onErrorReturn("Fallback response");
    }

    public Mono<String> demonstrateOnErrorResume() {
        return Mono.<String>error(new IllegalStateException("Primary operation failed"))
                .onErrorResume(IllegalStateException.class, error -> Mono.just(
                        "Recovered from: " + error.getMessage()));
    }

    public Mono<String> demonstrateOnErrorMap() {
        return Mono.<String>error(new IllegalStateException("Database operation failed"))
                .onErrorMap(IllegalStateException.class, error -> new ServiceOperationException(
                                "The product operation could not be completed", error));
    }

    public Mono<String> demonstrateDoOnError() {
        return Mono.<String>error(new IllegalStateException("Unexpected reactive failure"))
                .doOnError(error -> LOG.warn("Reactive operation failed: {}", error.getMessage()));
    }
}