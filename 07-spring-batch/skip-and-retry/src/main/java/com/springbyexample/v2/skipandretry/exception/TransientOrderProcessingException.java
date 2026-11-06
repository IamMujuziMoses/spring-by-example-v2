package com.springbyexample.v2.skipandretry.exception;

/**
 * Indicates a temporary failure while processing an order.
 *
 * @author Mujuzi Moses
 */
public class TransientOrderProcessingException extends RuntimeException {

    public TransientOrderProcessingException(String message) {
        super(message);
    }
}
