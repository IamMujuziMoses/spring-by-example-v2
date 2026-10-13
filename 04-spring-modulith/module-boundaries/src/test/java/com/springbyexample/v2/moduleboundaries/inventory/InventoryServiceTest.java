package com.springbyexample.v2.moduleboundaries.inventory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.moduleboundaries.catalog.api.Product;
import com.springbyexample.v2.moduleboundaries.catalog.api.ProductCatalog;

/**
 * @author Mujuzi Moses
 */
public class InventoryServiceTest {

    @Test
    void shouldFindProductThroughCatalogApi() {
        Product product = new Product("SKU-001", "Laptop");

        ProductCatalog catalog = sku -> "SKU-001".equals(sku) ? Optional.of(product) : Optional.empty();

        InventoryService inventoryService = new InventoryService(catalog);

        assertThat(inventoryService.findProduct("SKU-001")).contains(product);
    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExist() {
        ProductCatalog catalog = sku -> Optional.empty();

        InventoryService inventoryService = new InventoryService(catalog);

        assertThat(inventoryService.findProduct("UNKNOWN")).isEmpty();
    }
}
