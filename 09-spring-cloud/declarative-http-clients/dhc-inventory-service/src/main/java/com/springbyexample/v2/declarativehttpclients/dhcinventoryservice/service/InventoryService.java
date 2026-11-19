package com.springbyexample.v2.declarativehttpclients.dhcinventoryservice.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.declarativehttpclients.dhcinventoryservice.model.InventoryResponse;

/**
 * Handles inventory lookup operations.
 *
 * @author Mujuzi Moses
 */
@Service
public class InventoryService {

    private final Map<String, Integer> inventory = Map.of("SKU-1001", 25, "SKU-2002", 10, "SKU-3003", 5);

    /**
     * Retrieves the available quantity for a product.
     *
     * @param productId the product identifier
     * @return the inventory response
     */
    public InventoryResponse lookup(String productId) {
        int availableQuantity = inventory.getOrDefault(productId, 0);

        return new InventoryResponse(productId, availableQuantity);
    }
}
