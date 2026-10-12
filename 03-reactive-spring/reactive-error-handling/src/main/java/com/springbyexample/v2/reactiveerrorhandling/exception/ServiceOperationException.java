package com.springbyexample.v2.reactiveerrorhandling.exception;

/**
 * @author Mujuzi Moses
 */
public class ServiceOperationException extends RuntimeException {

    public ServiceOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
