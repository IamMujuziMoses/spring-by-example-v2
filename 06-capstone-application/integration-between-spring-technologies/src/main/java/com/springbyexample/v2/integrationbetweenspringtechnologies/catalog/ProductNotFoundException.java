package com.springbyexample.v2.integrationbetweenspringtechnologies.catalog;

/**
 * Thrown when a requested product does not exist or is inactive.
 *
 * @author Mujuzi Moses
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Product not found: " + productId);
    }
}
