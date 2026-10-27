package com.springbyexample.v2.applicationdesignandarchitecture.application.exception;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskId;

/**
 * Indicates that a requested task does not exist.
 *
 * @author Mujuzi Moses
 */
public class TaskNotFoundException extends RuntimeException {

    /**
     * Creates an exception for a missing task.
     *
     * @param id the missing task identifier
     */
    public TaskNotFoundException(TaskId id) {
        super("Task not found: " + id.value());
    }
}
