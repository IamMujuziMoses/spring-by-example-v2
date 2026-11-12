package com.springbyexample.v2.integrationtesting.model;

/**
 * Represents an incoming order message.
 *
 * @param orderId  unique order identifier
 * @param quantity number of items ordered
 *
 * @author Mujuzi Moses
 */
public record OrderMessage(String orderId, int quantity) {
}
