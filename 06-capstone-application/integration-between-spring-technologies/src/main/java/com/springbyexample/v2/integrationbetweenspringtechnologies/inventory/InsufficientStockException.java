package com.springbyexample.v2.integrationbetweenspringtechnologies.inventory;

/**
 * Thrown when an inventory reservation exceeds available stock.
 *
 * @author Mujuzi Moses
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long productId, int requestedQuantity) {

        super("Insufficient stock for product " + productId + ". Requested quantity: " + requestedQuantity);
    }
}
