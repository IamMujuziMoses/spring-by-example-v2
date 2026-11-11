package com.springbyexample.v2.errorchannels.model;

/**
 * Represents an order submitted for asynchronous processing.
 *
 * @param orderId  unique order identifier
 * @param quantity number of items ordered
 *
 * @author Mujuzi Moses
 */
public record OrderMessage(String orderId, int quantity) {
}
