package com.springbyexample.v2.declarativehttpclients.dhcorderservice.model;

/**
 * Represents the result of processing an order.
 *
 * @param orderId           the order identifier
 * @param productId         the product identifier
 * @param quantity          the requested quantity
 * @param availableQuantity the available inventory quantity
 * @param status            the order processing status
 *
 * @author Mujuzi Moses
 */
public record OrderResponse(String orderId, String productId, int quantity, int availableQuantity, String status) {
}
