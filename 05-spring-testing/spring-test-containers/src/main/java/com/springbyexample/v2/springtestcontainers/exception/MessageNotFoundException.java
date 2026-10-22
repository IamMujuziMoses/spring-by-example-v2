package com.springbyexample.v2.springtestcontainers.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Indicates that a requested message does not exist.
 *
 * @author Mujuzi Moses
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException(Long id) {
        super("Message not found: " + id);
    }
}
