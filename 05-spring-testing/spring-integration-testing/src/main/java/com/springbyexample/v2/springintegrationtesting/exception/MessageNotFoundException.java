package com.springbyexample.v2.springintegrationtesting.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Mujuzi Moses
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException(Long id) {
        super("Message not found: " + id);
    }
}
