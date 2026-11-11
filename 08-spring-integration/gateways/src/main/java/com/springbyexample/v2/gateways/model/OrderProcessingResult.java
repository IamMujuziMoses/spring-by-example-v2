package com.springbyexample.v2.gateways.model;

import java.math.BigDecimal;

/**
 * Represents the result of processing an order.
 *
 * @param orderId    the order identifier
 * @param product    the product name
 * @param quantity   the order quantity
 * @param totalPrice the calculated total price
 * @param source     the source of the order message
 *
 * @author Mujuzi Moses
 */
public record OrderProcessingResult(long orderId, String product, int quantity, BigDecimal totalPrice, String source) {
}
