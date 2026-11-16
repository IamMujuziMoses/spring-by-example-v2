package com.springbyexample.v2.apigateway.aginventoryservice;

/**
 * Response returned by the Inventory Service.
 *
 * @param service           the name of the backend service
 * @param sku               the requested stock keeping unit
 * @param availableQuantity the available inventory quantity
 *
 * @author Mujuzi Moses
 */
public record InventoryResponse(String service, String sku, int availableQuantity) {
}
