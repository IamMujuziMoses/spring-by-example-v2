package com.springbyexample.v2.declarativehttpclients.dhcintegrationtests.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springbyexample.v2.declarativehttpclients.dhcinventoryservice.model.InventoryResponse;
import com.springbyexample.v2.declarativehttpclients.dhcinventoryservice.service.InventoryService;

/**
 * Unit tests for InventoryService.
 *
 * @author Mujuzi Moses
 */
public class InventoryServiceTest {

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService();
    }

    @Test
    void shouldReturnAvailableQuantityForKnownProduct() {
        InventoryResponse response = inventoryService.lookup("SKU-1001");

        assertThat(response.productId()).isEqualTo("SKU-1001");
        assertThat(response.availableQuantity()).isEqualTo(25);
    }

    @Test
    void shouldReturnZeroForUnknownProduct() {
        InventoryResponse response = inventoryService.lookup("SKU-9999");

        assertThat(response.productId()).isEqualTo("SKU-9999");
        assertThat(response.availableQuantity()).isZero();
    }
}
