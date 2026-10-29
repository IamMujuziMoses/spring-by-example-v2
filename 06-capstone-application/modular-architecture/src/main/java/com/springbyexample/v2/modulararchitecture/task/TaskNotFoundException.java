package com.springbyexample.v2.modulararchitecture.task;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Indicates that a requested task does not exist.
 *
 * @author Mujuzi Moses
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long taskId) {
        super("Task not found with id: " + taskId);
    }
}