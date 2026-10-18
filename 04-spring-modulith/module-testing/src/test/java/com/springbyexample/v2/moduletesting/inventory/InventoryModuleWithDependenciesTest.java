package com.springbyexample.v2.moduletesting.inventory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;

import com.springbyexample.v2.moduletesting.catalog.CatalogItem;

/**
 * @author Mujuzi Moses
 */
@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
public class InventoryModuleWithDependenciesTest {

    @Autowired
    private InventoryService inventoryService;

    @Test
    void shouldReturnCatalogItemUsingRealCatalogModule() {
        CatalogItem result = inventoryService.findCatalogItem("SKU-001");

        assertThat(result.sku()).isEqualTo("SKU-001");
        assertThat(result.name()).isEqualTo("Sample item");
    }
}
