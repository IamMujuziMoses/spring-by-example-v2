package com.springbyexample.v2.distributedtracing.dtorderservice.model;

/**
 * Represents the inventory response returned by Inventory Service.
 *
 * @param productId         the product identifier
 * @param availableQuantity the available stock quantity
 * @param traceId           the distributed trace identifier
 *
 * @author Mujuzi Moses
 */
public record InventoryResponse(String productId, int availableQuantity, String traceId) {
}
