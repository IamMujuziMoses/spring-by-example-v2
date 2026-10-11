package com.springbyexample.v2.reactivecontrollers.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.reactivecontrollers.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@Service
public class ProductService {

    private final List<Product> products =
            List.of(
                    new Product(1L, "Laptop", "Electronics", 1200.00),
                    new Product(2L, "Keyboard", "Electronics", 75.00),
                    new Product(3L, "Notebook", "Stationery", 5.50)
            );

    public Flux<Product> findAll() {
        return Flux.fromIterable(products);
    }

    public Mono<Product> findById(Long id) {
        return Mono.justOrEmpty(products.stream().filter(product -> product.id().equals(id)).findFirst());
    }

    public Flux<Product> findByCategory(String category) {
        return Flux.fromIterable(products).filter(product -> product.category().equalsIgnoreCase(category));
    }
}
