package com.springbyexample.v2.messaging.msginventoryservice.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.messaging.msginventoryservice.event.OrderCreatedEvent;
import com.springbyexample.v2.messaging.msginventoryservice.model.InventoryItem;

/**
 * Handles inventory operations.
 *
 * @author Mujuzi Moses
 */
@Service
public class InventoryService {

    private final Map<String, Integer> inventory =
            new ConcurrentHashMap<>(Map.of("SKU-1001", 25, "SKU-2002", 10, "SKU-3003", 5));

    /**
     * Reduces available inventory when an order is created.
     *
     * @param event the order-created event
     */
    public void processOrder(OrderCreatedEvent event) {
        inventory.computeIfPresent(event.productId(), (productId, availableQuantity) ->
                        Math.max(0, availableQuantity - event.quantity()));
    }

    /**
     * Finds an inventory item by its product identifier.
     *
     * @param productId the product identifier
     * @return the inventory item, if it exists
     */
    public Optional<InventoryItem> findByProductId(String productId) {
        return Optional.ofNullable(inventory.get(productId))
                .map(quantity -> new InventoryItem(productId, quantity));
    }
}
