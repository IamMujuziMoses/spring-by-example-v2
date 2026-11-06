package com.springbyexample.v2.batchtesting.domain;

/**
 * Represents an order after batch processing.
 *
 * @param orderId     original order identifier
 * @param description processed order description
 * @author Mujuzi Moses
 */
public record ProcessedOrder(long orderId, String description) {
}
