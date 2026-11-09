package com.springbyexample.v2.messageendpoints.domain;

/**
 * Represents an order message processed by a message endpoint.
 *
 * @param orderId     the order identifier
 * @param description the order description
 *
 * @author Mujuzi Moses
 */
public record OrderMessage(long orderId, String description) {
}
