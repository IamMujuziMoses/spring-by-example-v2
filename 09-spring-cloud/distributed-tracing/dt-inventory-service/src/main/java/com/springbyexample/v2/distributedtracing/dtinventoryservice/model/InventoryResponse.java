package com.springbyexample.v2.distributedtracing.dtinventoryservice.model;

/**
 * Represents the result of an inventory lookup.
 *
 * @param productId         the product identifier
 * @param availableQuantity the available stock quantity
 * @param traceId           the distributed trace identifier
 *
 * @author Mujuzi Moses
 */
public record InventoryResponse(String productId, int availableQuantity, String traceId) {
}
