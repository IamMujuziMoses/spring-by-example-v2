package com.springbyexample.v2.toolcalling.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles exceptions raised during AI tool calling operations.
 *
 * @author Mujuzi Moses
 */
@RestControllerAdvice(assignableTypes = ToolCallingController.class)
public class ToolCallingExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "The AI service could not complete the requested operation.");

        problemDetail.setTitle("Tool Calling Operation Failed");
        problemDetail.setType(URI.create("urn:problem:tool-calling-operation-failed"));

        return problemDetail;
    }
}
