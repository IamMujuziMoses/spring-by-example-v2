package com.springbyexample.v2.moduleboundaries.catalog.internal;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.moduleboundaries.catalog.api.Product;
import com.springbyexample.v2.moduleboundaries.catalog.api.ProductCatalog;

/**
 * @author Mujuzi Moses
 */
@Service
public class ProductCatalogImpl implements ProductCatalog {

    private final Map<String, Product> products = Map.of(
            "SKU-001", new Product("SKU-001", "Laptop"),
            "SKU-002", new Product("SKU-002", "Keyboard"),
            "SKU-003", new Product("SKU-003", "Monitor")
    );

    @Override
    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(products.get(sku));
    }
}
