package com.springbyexample.v2.integrationbetweenspringtechnologies.ordering;

/**
 * Thrown when an order cannot be found for the requested customer.
 *
 * @author Mujuzi Moses
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found: " + orderId);
    }
}
