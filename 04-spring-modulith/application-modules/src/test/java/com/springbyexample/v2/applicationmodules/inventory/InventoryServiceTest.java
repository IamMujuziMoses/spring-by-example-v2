package com.springbyexample.v2.applicationmodules.inventory;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author Mujuzi Moses
 */
public class InventoryServiceTest {

    private final InventoryService inventoryService = new InventoryService();

    @Test
    void shouldReturnAvailableQuantityForSku() {
        assertThat(inventoryService.getAvailableQuantity("SKU-100")).isEqualTo(10);
    }

    @Test
    void shouldRejectBlankSku() {
        assertThatIllegalArgumentException().isThrownBy(() -> inventoryService.getAvailableQuantity(" "));
    }
}
