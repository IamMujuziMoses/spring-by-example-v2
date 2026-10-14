package com.springbyexample.v2.applicationevents.catalog.internal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springbyexample.v2.applicationevents.catalog.Product;

/**
 * @author Mujuzi Moses
 */
public class ProductRepositoryTest {

    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository = new ProductRepository();
    }

    @Test
    void save_shouldSaveAndFindProduct() {
        Product product = new Product("SKU-003", "Monitor");

        Product savedProduct = productRepository.save(product);

        assertThat(savedProduct).isSameAs(product);
        assertThat(productRepository.findBySku("SKU-003")).contains(product);
    }

    @Test
    void findBySku_shouldReturnEmptyWhenProductDoesNotExist() {
        assertThat(productRepository.findBySku("SKU-UNKNOWN")).isEmpty();
    }

    @Test
    void findBySku_shouldFindProductsAddedDuringInitialization() {
        assertThat(productRepository.findBySku("SKU-001")).contains(new Product("SKU-001", "Laptop"));

        assertThat(productRepository.findBySku("SKU-002")).contains(new Product("SKU-002", "Keyboard"));
    }
}
