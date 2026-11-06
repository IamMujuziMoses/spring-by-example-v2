package com.springbyexample.v2.skipandretry;

/**
 * Represents an order that has been successfully processed.
 *
 * @param orderId     the original order identifier
 * @param description the processed order description
 * @author Mujuzi Moses
 */
public record ProcessedOrder(long orderId, String description) {
}
