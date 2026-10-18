package com.springbyexample.v2.moduledocumentation.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.moduledocumentation.catalog.CatalogItem;
import com.springbyexample.v2.moduledocumentation.catalog.CatalogService;

/**
 * @author Mujuzi Moses
 */
public class InventoryServiceTest {

    private final CatalogService catalogService = mock(CatalogService.class);

    private final InventoryService inventoryService = new InventoryService(catalogService);

    @Test
    void findCatalogItem_shouldReturnCatalogItemForSku() {
        CatalogItem expected = new CatalogItem("SKU-001", "Sample item");
        when(catalogService.findBySku("SKU-001")).thenReturn(expected);

        CatalogItem result = inventoryService.findCatalogItem("SKU-001");

        assertEquals(expected, result);
        verify(catalogService).findBySku("SKU-001");
    }
}
