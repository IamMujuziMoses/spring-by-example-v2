package com.springbyexample.v2.prompttemplates.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles exceptions raised while processing prompt template requests.
 *
 * @author Mujuzi Moses
 */
@RestControllerAdvice(assignableTypes = PromptTemplateController.class)
public class PromptTemplateExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException exception) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, "The chat model is currently unable to process the request.");
    }
}
