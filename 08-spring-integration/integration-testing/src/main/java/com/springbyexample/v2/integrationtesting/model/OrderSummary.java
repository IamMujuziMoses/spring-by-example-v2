package com.springbyexample.v2.integrationtesting.model;

/**
 * Represents a summary of a processed order.
 *
 * @param orderId     unique order identifier
 * @param quantity    number of items ordered
 * @param description order description
 *
 * @author Mujuzi Moses
 */
public record OrderSummary(String orderId, int quantity, String description) {
}
