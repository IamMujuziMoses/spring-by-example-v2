package com.springbyexample.v2.structuredoutput.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles errors raised during structured output generation.
 *
 * @author Mujuzi Moses
 */
@RestControllerAdvice(assignableTypes = StructuredOutputController.class)
public class StructuredOutputExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleStructuredOutputException(IllegalStateException exception) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "The AI model returned a response that could not be processed.");

        problemDetail.setTitle("Structured Output Generation Failed");

        return problemDetail;
    }
}
