package com.springbyexample.v2.moduleboundaries.catalog.internal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.moduleboundaries.catalog.api.Product;

/**
 * @author Mujuzi Moses
 */
public class ProductCatalogImplTest {

    private final ProductCatalogImpl productCatalog = new ProductCatalogImpl();

    @Test
    void shouldFindProductBySku() {
        assertThat(productCatalog.findBySku("SKU-001")).contains(new Product("SKU-001", "Laptop"));
    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExist() {
        assertThat(productCatalog.findBySku("UNKNOWN")).isEmpty();
    }
}

