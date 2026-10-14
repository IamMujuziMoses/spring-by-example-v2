package com.springbyexample.v2.applicationevents.inventory;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.applicationevents.catalog.ProductRegisteredEvent;

/**
 * @author Mujuzi Moses
 */
@Component
public class InventoryEventListener {

    private final InventoryService inventoryService;

    public InventoryEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @EventListener
    public void on(ProductRegisteredEvent event) {
        inventoryService.recordRegisteredProduct(event.sku());
    }
}

