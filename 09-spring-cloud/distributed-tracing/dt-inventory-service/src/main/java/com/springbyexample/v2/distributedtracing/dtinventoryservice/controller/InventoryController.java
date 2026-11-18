package com.springbyexample.v2.distributedtracing.dtinventoryservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.distributedtracing.dtinventoryservice.model.InventoryResponse;
import com.springbyexample.v2.distributedtracing.dtinventoryservice.service.InventoryService;

/**
 * Exposes inventory lookup endpoints.
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
    public InventoryResponse getInventory(@PathVariable String productId) {
        return inventoryService.lookup(productId);
    }
}
