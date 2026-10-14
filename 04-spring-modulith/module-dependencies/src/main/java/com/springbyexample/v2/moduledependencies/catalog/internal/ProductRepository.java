package com.springbyexample.v2.moduledependencies.catalog.internal;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.springbyexample.v2.moduledependencies.catalog.Product;

/**
 * @author Mujuzi Moses
 */
@Repository
public class ProductRepository {

    private final Map<String, Product> products = Map.of(
            "SKU-001", new Product("SKU-001", "Laptop"),
            "SKU-002", new Product("SKU-002", "Keyboard"),
            "SKU-003", new Product("SKU-003", "Monitor")
    );

    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(products.get(sku));
    }
}
