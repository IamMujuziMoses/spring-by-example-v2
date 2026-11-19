package com.springbyexample.v2.messaging.msginventoryservice.event;

import java.time.Instant;

/**
 * Represents an order-created event received from Order Service.
 *
 * @param eventId    the unique event identifier
 * @param orderId    the order identifier
 * @param productId  the product identifier
 * @param quantity   the requested quantity
 * @param occurredAt the time the event occurred
 *
 * @author Mujuzi Moses
 */
public record OrderCreatedEvent(String eventId, String orderId, String productId, int quantity, Instant occurredAt) {
}
