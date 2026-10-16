package com.springbyexample.v2.moduleverification.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.moduleverification.catalog.CatalogItem;
import com.springbyexample.v2.moduleverification.catalog.CatalogService;

/**
 * @author Mujuzi Moses
 */
public class InventoryServiceTest {

    private final CatalogService catalogService = mock(CatalogService.class);

    private final InventoryService inventoryService = new InventoryService(catalogService);

    @Test
    void findBySku_shouldReturnCatalogItemForSku() {
        CatalogItem expected = new CatalogItem("SKU-001", "Sample item");

        when(catalogService.findBySku("SKU-001")).thenReturn(expected);

        CatalogItem actual = inventoryService.findCatalogItem("SKU-001");

        assertEquals(expected, actual);
        verify(catalogService).findBySku("SKU-001");
    }
}