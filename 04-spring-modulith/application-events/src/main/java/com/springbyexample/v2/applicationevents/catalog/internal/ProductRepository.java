package com.springbyexample.v2.applicationevents.catalog.internal;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.springbyexample.v2.applicationevents.catalog.Product;

/**
 * @author Mujuzi Moses
 */
@Repository
public class ProductRepository {

    private final Map<String, Product> products = new ConcurrentHashMap<>();

    public ProductRepository() {
        products.put("SKU-001", new Product("SKU-001", "Laptop"));
        products.put("SKU-002", new Product("SKU-002", "Keyboard"));
    }

    public Product save(Product product) {
        products.put(product.sku(), product);
        return product;
    }

    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(products.get(sku));
    }
}

