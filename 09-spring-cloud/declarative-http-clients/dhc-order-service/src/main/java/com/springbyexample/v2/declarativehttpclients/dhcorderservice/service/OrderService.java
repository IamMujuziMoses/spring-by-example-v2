package com.springbyexample.v2.declarativehttpclients.dhcorderservice.service;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.declarativehttpclients.dhcorderservice.client.InventoryClient;
import com.springbyexample.v2.declarativehttpclients.dhcorderservice.model.InventoryResponse;
import com.springbyexample.v2.declarativehttpclients.dhcorderservice.model.OrderRequest;
import com.springbyexample.v2.declarativehttpclients.dhcorderservice.model.OrderResponse;

/**
 * Handles order processing operations.
 *
 * @author Mujuzi Moses
 */
@Service
public class OrderService {

    private final InventoryClient inventoryClient;

    public OrderService(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    /**
     * Processes an order by checking available inventory.
     *
     * @param request the order request
     * @return the order processing result
     */
    public OrderResponse process(OrderRequest request) {
        InventoryResponse inventory = inventoryClient.getInventory(request.productId());

        if (inventory == null) {
            throw new IllegalStateException("Inventory Service returned an empty response");
        }

        String status = inventory.availableQuantity() >= request.quantity() ? "ACCEPTED" : "REJECTED";

        return new OrderResponse(request.orderId(), request.productId(), request.quantity(),
                inventory.availableQuantity(), status);
    }
}
