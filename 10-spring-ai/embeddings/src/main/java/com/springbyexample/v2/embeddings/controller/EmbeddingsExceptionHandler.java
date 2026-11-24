package com.springbyexample.v2.embeddings.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles exceptions raised by the embeddings API.
 *
 * @author Mujuzi Moses
 */
@RestControllerAdvice(assignableTypes = EmbeddingsController.class)
public class EmbeddingsExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException exception) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "The embedding model returned a response that could not be processed.");

        problemDetail.setTitle("Embedding Generation Failed");

        return problemDetail;
    }
}
