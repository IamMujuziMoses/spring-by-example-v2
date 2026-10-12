package com.springbyexample.v2.reactiveerrorhandling.exception;

/**
 * @author Mujuzi Moses
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Product not found with ID: " + id);
    }
}