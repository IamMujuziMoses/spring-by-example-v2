package com.springbyexample.v2.restapis.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.springbyexample.v2.applicationdesignandarchitecture.application.exception.ProjectNotFoundException;
import com.springbyexample.v2.applicationdesignandarchitecture.application.exception.TaskNotFoundException;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.exception.TaskTransitionException;

/**
 * Handles exceptions raised by REST API endpoints.
 *
 * @author Mujuzi Moses
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles missing projects and tasks.
     *
     * @param exception exception describing the missing resource
     * @return a not-found response
     */
    @ExceptionHandler( { ProjectNotFoundException.class, TaskNotFoundException.class } )
    public ResponseEntity<ApiError> handleNotFound(RuntimeException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(exception.getMessage()));
    }

    /**
     * Handles invalid task status transitions.
     *
     * @param exception exception describing the invalid transition
     * @return a bad-request response
     */
    @ExceptionHandler(TaskTransitionException.class)
    public ResponseEntity<ApiError> handleInvalidTransition(TaskTransitionException exception) {

        return ResponseEntity.badRequest().body(new ApiError(exception.getMessage()));
    }

    /**
     * Handles request validation failures.
     *
     * @param exception validation exception
     * @return a bad-request response with validation details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {

        List<String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity.badRequest().body(new ApiError("Request validation failed", details));
    }

    /**
     * Handles malformed JSON and invalid JSON values.
     *
     * @param exception message conversion exception
     * @return a bad-request response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableRequest(HttpMessageNotReadableException exception) {

        return ResponseEntity.badRequest().body(new ApiError("Request body is malformed or contains invalid values"));
    }

    /**
     * Represents a REST API error response.
     *
     * @param message error message
     * @param details additional error details
     */
    public record ApiError(String message, List<String> details) {

        public ApiError(String message) {
            this(message, List.of());
        }
    }
}