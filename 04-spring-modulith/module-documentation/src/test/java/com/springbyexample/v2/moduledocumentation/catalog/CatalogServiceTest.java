package com.springbyexample.v2.moduledocumentation.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.moduledocumentation.catalog.internal.CatalogStore;

/**
 * @author Mujuzi Moses
 */
public class CatalogServiceTest {

    private final CatalogStore catalogStore = mock(CatalogStore.class);

    private final CatalogService catalogService = new CatalogService(catalogStore);

    @Test
    void findBySku_shouldReturnCatalogItemForSku() {
        when(catalogStore.findNameBySku("SKU-001")).thenReturn("Sample item");

        CatalogItem result = catalogService.findBySku("SKU-001");

        assertEquals("SKU-001", result.sku());
        assertEquals("Sample item", result.name());

        verify(catalogStore).findNameBySku("SKU-001");
    }
}
