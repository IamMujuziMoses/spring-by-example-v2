package com.springbyexample.v2.gateways.model;

import java.math.BigDecimal;

/**
 * Represents an incoming order message.
 *
 * @param orderId   the order identifier
 * @param product   the product name
 * @param quantity  the order quantity
 * @param unitPrice the price per item
 *
 * @author Mujuzi Moses
 */
public record OrderMessage(long orderId, String product, int quantity, BigDecimal unitPrice) {
}
