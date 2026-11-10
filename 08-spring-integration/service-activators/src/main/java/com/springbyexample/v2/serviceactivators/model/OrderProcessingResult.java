package com.springbyexample.v2.serviceactivators.model;

import java.math.BigDecimal;

/**
 * Represents the result of processing an order.
 *
 * @param orderId    the order identifier
 * @param product    the processed product
 * @param quantity   the processed quantity
 * @param totalPrice the calculated order total
 *
 * @author Mujuzi Moses
 */
public record OrderProcessingResult(long orderId, String product, int quantity, BigDecimal totalPrice) {
}
