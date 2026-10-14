package com.springbyexample.v2.moduledependencies.catalog;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.moduledependencies.catalog.internal.ProductRepository;

/**
 * @author Mujuzi Moses
 */
@Service
public class CatalogService {

    private final ProductRepository productRepository;

    public CatalogService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }
}
