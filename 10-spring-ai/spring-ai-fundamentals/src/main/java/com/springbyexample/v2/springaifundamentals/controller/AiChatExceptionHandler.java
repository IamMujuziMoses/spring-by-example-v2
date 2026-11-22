package com.springbyexample.v2.springaifundamentals.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles exceptions raised by AI chat operations.
 *
 * @author Mujuzi Moses
 */
@RestControllerAdvice(assignableTypes = ChatController.class)
public class AiChatExceptionHandler {

    /**
     * Handles failures when communicating with the AI service.
     *
     * @return a service unavailable response
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleAiServiceFailure() {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, "The AI service is temporarily unavailable.");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problemDetail);
    }
}
