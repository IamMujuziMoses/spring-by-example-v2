package com.springbyexample.v2.filters.model;

/**
 * Represents an order message.
 *
 * @param orderId  the order identifier
 * @param product  the product name
 * @param quantity the ordered quantity
 *
 * @author Mujuzi Moses
 */
public record OrderMessage(long orderId, String product, int quantity) {
}
