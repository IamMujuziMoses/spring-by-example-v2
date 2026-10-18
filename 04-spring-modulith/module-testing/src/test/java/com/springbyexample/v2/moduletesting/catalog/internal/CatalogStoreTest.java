package com.springbyexample.v2.moduletesting.catalog.internal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Mujuzi Moses
 */
public class CatalogStoreTest {

    private CatalogStore catalogStore;

    @BeforeEach
    void setUp() {
        catalogStore = new CatalogStore();
    }

    @Test
    void shouldReturnSampleItemName() {
        String result = catalogStore.findNameBySku("SKU-001");

        assertThat(result).isEqualTo("Sample item");
    }
}

