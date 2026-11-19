package com.springbyexample.v2.messaging.msginventoryservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.messaging.msginventoryservice.model.InventoryItem;
import com.springbyexample.v2.messaging.msginventoryservice.service.InventoryService;

/**
 * Exposes inventory query endpoints.
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
     * Retrieves inventory for a product.
     *
     * @param productId the product identifier
     * @return the inventory item, or 404 if it does not exist
     */
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryItem> findByProductId(@PathVariable String productId) {
        return inventoryService.findByProductId(productId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
