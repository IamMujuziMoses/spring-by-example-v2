package com.springbyexample.v2.distributedtracing.dtorderservice.model;

/**
 * Represents the result of processing an order.
 *
 * @param orderId           the unique order identifier
 * @param productId         the product identifier
 * @param quantity          the requested quantity
 * @param availableQuantity the remaining available quantity
 * @param status            the order processing status
 * @param traceId           the distributed trace identifier
 * @param inventoryTraceId  the trace identifier observed by Inventory Service
 *
 * @author Mujuzi Moses
 */
public record OrderResponse(String orderId, String productId, int quantity, int availableQuantity, String status,
        String traceId, String inventoryTraceId) {
}

