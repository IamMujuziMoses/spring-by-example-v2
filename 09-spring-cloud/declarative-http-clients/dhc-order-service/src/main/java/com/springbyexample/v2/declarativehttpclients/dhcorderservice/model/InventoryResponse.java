package com.springbyexample.v2.declarativehttpclients.dhcorderservice.model;

/**
 * Represents the inventory information for a product.
 *
 * @param productId         the product identifier
 * @param availableQuantity the available stock quantity
 *
 * @author Mujuzi Moses
 */
public record InventoryResponse(String productId, int availableQuantity) {
}
