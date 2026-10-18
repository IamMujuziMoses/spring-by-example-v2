package com.springbyexample.v2.moduletesting.inventory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.springbyexample.v2.moduletesting.catalog.CatalogItem;
import com.springbyexample.v2.moduletesting.catalog.CatalogService;

/**
 * @author Mujuzi Moses
 */
@ApplicationModuleTest
public class InventoryModuleIsolatedTest {

    @Autowired
    private InventoryService inventoryService;

    @MockitoBean
    private CatalogService catalogService;

    @Test
    void shouldReturnCatalogItemUsingMockedCatalogModule() {
        when(catalogService.findBySku("SKU-001")).thenReturn(new CatalogItem("SKU-001", "Test item"));

        CatalogItem result = inventoryService.findCatalogItem("SKU-001");

        assertThat(result).isEqualTo(new CatalogItem("SKU-001", "Test item"));
    }
}
