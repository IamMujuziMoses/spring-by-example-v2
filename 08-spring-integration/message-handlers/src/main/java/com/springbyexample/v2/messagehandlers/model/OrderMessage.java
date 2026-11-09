package com.springbyexample.v2.messagehandlers.model;

/**
 * Represents an order message.
 *
 * @param orderId     the order identifier
 * @param description the order description
 *
 * @author Mujuzi Moses
 */
public record OrderMessage(long orderId, String description) {
}
