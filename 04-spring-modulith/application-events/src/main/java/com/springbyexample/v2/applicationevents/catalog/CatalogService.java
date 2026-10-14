package com.springbyexample.v2.applicationevents.catalog;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.springbyexample.v2.applicationevents.catalog.internal.ProductRepository;

/**
 * @author Mujuzi Moses
 */
@Service
public class CatalogService {

    private final ProductRepository productRepository;

    private final ApplicationEventPublisher eventPublisher;

    public CatalogService(ProductRepository productRepository, ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    public Product registerProduct(String sku, String name) {
        Product product = new Product(sku, name);

        productRepository.save(product);

        eventPublisher.publishEvent(new ProductRegisteredEvent(product.sku(), product.name()));

        return product;
    }

    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }
}

