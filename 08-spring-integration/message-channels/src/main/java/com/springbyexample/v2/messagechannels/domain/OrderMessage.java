package com.springbyexample.v2.messagechannels.domain;

/**
 * Represents an order message exchanged through Spring Integration channels.
 *
 * @param orderId     order identifier
 * @param description order description
 *
 * @author Mujuzi Moses
 */
public record OrderMessage(long orderId, String description) {
}
