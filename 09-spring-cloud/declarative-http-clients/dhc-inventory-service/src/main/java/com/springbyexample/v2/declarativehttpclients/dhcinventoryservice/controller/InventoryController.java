package com.springbyexample.v2.declarativehttpclients.dhcinventoryservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.declarativehttpclients.dhcinventoryservice.model.InventoryResponse;
import com.springbyexample.v2.declarativehttpclients.dhcinventoryservice.service.InventoryService;

/**
 * REST controller exposing inventory endpoints.
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

    /**
     * Retrieves inventory information for a product.
     *
     * @param productId the product identifier
     * @return the inventory response
     */
    @GetMapping("/{productId}")
    public InventoryResponse getInventory(@PathVariable String productId) {
        return inventoryService.lookup(productId);
    }
}
