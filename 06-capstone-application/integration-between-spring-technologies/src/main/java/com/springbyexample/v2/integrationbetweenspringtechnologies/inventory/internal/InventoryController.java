package com.springbyexample.v2.integrationbetweenspringtechnologies.inventory.internal;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.integrationbetweenspringtechnologies.inventory.InventoryService;

/**
 * REST controller for inventory availability.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public Map<String, Object> getInventory(@PathVariable Long productId) {

        return Map.of("productId", productId, "availableQuantity", inventoryService.getAvailableQuantity(productId));
    }
}
