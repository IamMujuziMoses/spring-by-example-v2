package com.springbyexample.v2.routers.model;

/**
 * Represents an order message.
 *
 * @param orderId   the order identifier
 * @param product   the ordered product
 * @param quantity  the ordered quantity
 * @param orderType the order type
 *
 * @author Mujuzi Moses
 */
public record OrderMessage(long orderId, String product, int quantity, OrderType orderType) {
}
