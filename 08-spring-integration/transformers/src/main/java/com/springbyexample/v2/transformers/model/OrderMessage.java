package com.springbyexample.v2.transformers.model;

import java.math.BigDecimal;

/**
 * Represents an incoming order message.
 *
 * @param orderId   the order identifier
 * @param product   the ordered product
 * @param quantity  the ordered quantity
 * @param unitPrice the price per unit
 *
 * @author Mujuzi Moses
 */
public record OrderMessage(long orderId, String product, int quantity, BigDecimal unitPrice) {
}
