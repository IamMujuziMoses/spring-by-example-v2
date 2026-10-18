package com.springbyexample.v2.moduletesting.inventory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.springbyexample.v2.moduletesting.catalog.CatalogItem;
import com.springbyexample.v2.moduletesting.catalog.CatalogService;

/**
 * @author Mujuzi Moses
 */
@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private CatalogService catalogService;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(catalogService);
    }

    @Test
    void shouldReturnCatalogItemFromCatalogService() {
        CatalogItem expected = new CatalogItem("SKU-001", "Sample item");

        when(catalogService.findBySku("SKU-001")).thenReturn(expected);

        CatalogItem result = inventoryService.findCatalogItem("SKU-001");

        assertThat(result).isEqualTo(expected);
        verify(catalogService).findBySku("SKU-001");
    }
}

