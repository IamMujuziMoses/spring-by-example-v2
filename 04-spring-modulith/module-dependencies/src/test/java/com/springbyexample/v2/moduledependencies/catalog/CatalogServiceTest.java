package com.springbyexample.v2.moduledependencies.catalog;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.moduledependencies.catalog.internal.ProductRepository;

/**
 * @author Mujuzi Moses
 */
public class CatalogServiceTest {

    private final CatalogService catalogService = new CatalogService(new ProductRepository());

    @Test
    void findBySku_shouldFindProductBySku() {
        assertThat(catalogService.findBySku("SKU-001")).contains(new Product("SKU-001", "Laptop"));
    }

    @Test
    void findBySku_shouldReturnEmptyWhenProductDoesNotExist() {
        assertThat(catalogService.findBySku("UNKNOWN")).isEmpty();
    }
}
