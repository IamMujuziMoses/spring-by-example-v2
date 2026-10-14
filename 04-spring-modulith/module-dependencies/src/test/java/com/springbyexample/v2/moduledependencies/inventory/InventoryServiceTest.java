package com.springbyexample.v2.moduledependencies.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.moduledependencies.catalog.CatalogService;
import com.springbyexample.v2.moduledependencies.catalog.Product;
import com.springbyexample.v2.moduledependencies.catalog.internal.ProductRepository;

/**
 * @author Mujuzi Moses
 */
public class InventoryServiceTest {

    @Test
    void findProduct_shouldFindProductThroughAllowedCatalogDependency() {
        Product product = new Product("SKU-001", "Laptop");

        ProductRepository productRepository = mock(ProductRepository.class);
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));

        CatalogService catalogService = new CatalogService(productRepository);
        InventoryService inventoryService = new InventoryService(catalogService);

        assertThat(inventoryService.findProduct("SKU-001")).contains(product);
    }


    @Test
    void findProduct_shouldReturnEmptyWhenProductDoesNotExist() {
        ProductRepository productRepository = mock(ProductRepository.class);
        when(productRepository.findBySku("UNKNOWN")).thenReturn(Optional.empty());

        CatalogService catalogService = new CatalogService(productRepository);
        InventoryService inventoryService = new InventoryService(catalogService);

        assertThat(inventoryService.findProduct("UNKNOWN")).isEmpty();
    }
}
