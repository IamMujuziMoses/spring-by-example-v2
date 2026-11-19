package com.springbyexample.v2.declarativehttpclients.dhcorderservice.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.springbyexample.v2.declarativehttpclients.dhcorderservice.model.InventoryResponse;

/**
 * Declarative HTTP client for Inventory Service.
 *
 * @author Mujuzi Moses
 */
@HttpExchange("/api/inventory")
public interface InventoryClient {

    /**
     * Retrieves inventory information for a product.
     *
     * @param productId the product identifier
     * @return the inventory response
     */
    @GetExchange("/{productId}")
    InventoryResponse getInventory(@PathVariable("productId") String productId);
}
