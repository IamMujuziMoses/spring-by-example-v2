package com.springbyexample.v2.retrievalaugmentedgeneration.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles exceptions raised by Retrieval-Augmented Generation operations.
 *
 * @author Mujuzi Moses
 */
@RestControllerAdvice(assignableTypes = RagController.class)
public class RagExceptionHandler {

    /**
     * Handles failures during document ingestion or answer generation.
     *
     * @param exception the exception raised
     * @return a problem detail response
     */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "The AI service could not complete the requested operation.");

        problemDetail.setTitle("RAG Operation Failed");
        problemDetail.setType(URI.create("urn:problem:rag-operation-failed"));

        return problemDetail;
    }
}
