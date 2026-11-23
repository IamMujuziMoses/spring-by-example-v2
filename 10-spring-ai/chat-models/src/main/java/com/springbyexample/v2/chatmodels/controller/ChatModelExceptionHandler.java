package com.springbyexample.v2.chatmodels.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles exceptions raised while processing chat model requests.
 *
 * @author Mujuzi Moses
 */
@RestControllerAdvice(assignableTypes = ChatModelController.class)
public class ChatModelExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException exception) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, "The chat model is currently unable to process the request.");
    }
}
