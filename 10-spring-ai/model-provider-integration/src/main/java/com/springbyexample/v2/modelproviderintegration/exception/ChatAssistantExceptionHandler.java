package com.springbyexample.v2.modelproviderintegration.exception;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles exceptions raised during AI assistant operations.
 *
 * @author Mujuzi Moses
 */
@RestControllerAdvice
public class ChatAssistantExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "The AI service could not complete the requested operation.");

        problemDetail.setTitle("AI Assistant Operation Failed");
        problemDetail.setType(URI.create("urn:problem:ai-assistant-operation-failed"));

        return problemDetail;
    }
}
