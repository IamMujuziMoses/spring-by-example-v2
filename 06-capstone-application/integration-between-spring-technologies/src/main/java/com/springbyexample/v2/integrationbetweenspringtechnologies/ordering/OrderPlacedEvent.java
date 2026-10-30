package com.springbyexample.v2.integrationbetweenspringtechnologies.ordering;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Published when a customer successfully places an order.
 *
 * @author Mujuzi Moses
 */
public record OrderPlacedEvent(Long orderId, String customerUsername, BigDecimal totalAmount, Instant occurredAt) {
}
