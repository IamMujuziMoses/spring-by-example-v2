package com.springbyexample.v2.vectorstores.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles vector store operation failures.
 *
 * @author Mujuzi Moses
 */
@RestControllerAdvice(assignableTypes = VectorStoreController.class)
public class VectorStoreExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException exception) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "The vector store operation could not be completed.");

        problemDetail.setTitle("Vector Store Operation Failed");

        return problemDetail;
    }
}
