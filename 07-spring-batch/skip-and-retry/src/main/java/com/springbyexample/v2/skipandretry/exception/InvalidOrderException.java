package com.springbyexample.v2.skipandretry.exception;

/**
 * Indicates that an order contains invalid data and should be skipped.
 *
 * @author Mujuzi Moses
 */
public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
