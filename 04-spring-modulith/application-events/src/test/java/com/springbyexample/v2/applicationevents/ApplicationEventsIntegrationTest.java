package com.springbyexample.v2.applicationevents;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.applicationevents.catalog.CatalogService;
import com.springbyexample.v2.applicationevents.inventory.InventoryService;

/**
 * @author Mujuzi Moses
 */
@SpringBootTest
public class ApplicationEventsIntegrationTest {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private InventoryService inventoryService;

    @Test
    void shouldNotifyInventoryWhenProductIsRegistered() {
        String sku = "SKU-EVENT-001";

        catalogService.registerProduct(sku, "External Drive");

        assertThat(inventoryService.containsSku(sku)).isTrue();
    }
}

