package com.springbyexample.v2.moduletesting.catalog;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.springbyexample.v2.moduletesting.catalog.internal.CatalogStore;

/**
 * @author Mujuzi Moses
 */
@ExtendWith(MockitoExtension.class)
public class CatalogServiceTest {

    @Mock
    private CatalogStore catalogStore;

    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        catalogService = new CatalogService(catalogStore);
    }

    @Test
    void shouldReturnCatalogItemWhenSkuExists() {
        when(catalogStore.findNameBySku("SKU-001")).thenReturn("Sample item");

        CatalogItem result = catalogService.findBySku("SKU-001");

        assertThat(result).isEqualTo(new CatalogItem("SKU-001", "Sample item"));

        verify(catalogStore).findNameBySku("SKU-001");
    }
}

