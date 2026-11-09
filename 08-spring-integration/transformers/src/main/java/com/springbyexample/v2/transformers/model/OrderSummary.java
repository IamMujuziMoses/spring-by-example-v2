package com.springbyexample.v2.transformers.model;

import java.math.BigDecimal;

/**
 * Represents a transformed order summary.
 *
 * @param orderId     the order identifier
 * @param description the order description
 * @param totalPrice  the calculated total price
 *
 * @author Mujuzi Moses
 */
public record OrderSummary(long orderId, String description, BigDecimal totalPrice) {
}
