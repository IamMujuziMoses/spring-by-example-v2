package com.springbyexample.v2.moduledocumentation.catalog.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author Mujuzi Moses
 */
public class CatalogStoreTest {

    private final CatalogStore catalogStore = new CatalogStore();

    @Test
    void findNameBySku_shouldReturnSampleItemName() {
        String result = catalogStore.findNameBySku("SKU-001");

        assertEquals("Sample item", result);
    }
}
