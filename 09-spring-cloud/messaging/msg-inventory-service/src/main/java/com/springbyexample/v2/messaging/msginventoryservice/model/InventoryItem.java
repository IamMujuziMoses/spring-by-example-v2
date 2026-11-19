package com.springbyexample.v2.messaging.msginventoryservice.model;

/**
 * Represents an inventory item.
 *
 * @param productId         the product identifier
 * @param availableQuantity the available quantity
 *
 * @author Mujuzi Moses
 */
public record InventoryItem(String productId, int availableQuantity) {
}
