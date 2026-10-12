package com.springbyexample.v2.reactiveerrorhandling.exception;

/**
 * @author Mujuzi Moses
 */
public class InvalidProductIdException extends RuntimeException {

    public InvalidProductIdException(Long id) {
        super("Product ID must be positive: " + id);
    }
}
