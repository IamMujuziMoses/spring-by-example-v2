package com.springbyexample.v2.applicationmodules.inventory;

import org.springframework.stereotype.Service;

/**
 * @author Mujuzi Moses
 */
@Service
public class InventoryService {

    public int getAvailableQuantity(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank");
        }

        return 10;
    }
}
