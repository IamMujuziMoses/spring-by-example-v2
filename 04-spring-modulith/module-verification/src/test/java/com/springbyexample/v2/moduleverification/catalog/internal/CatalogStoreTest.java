package com.springbyexample.v2.moduleverification.catalog.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.moduleverification.catalog.interrnal.CatalogStore;

/**
 * @author Mujuzi Moses
 */
public class CatalogStoreTest {

    private final CatalogStore catalogStore = new CatalogStore();

    @Test
    void findNameBySku_shouldReturnNameForSku() {
        String name = catalogStore.findNameBySku("SKU-001");

        assertEquals("Sample item", name);
    }
}
