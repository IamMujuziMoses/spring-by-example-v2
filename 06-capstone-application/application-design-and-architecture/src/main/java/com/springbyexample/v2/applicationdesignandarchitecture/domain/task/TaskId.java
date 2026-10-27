package com.springbyexample.v2.applicationdesignandarchitecture.domain.task;

import java.util.Objects;

/**
 * Represents the unique identifier of a task.
 *
 * @author Mujuzi Moses
 */
public record TaskId(String value) {

    /**
     * Creates a task identifier.
     *
     * @param value the identifier value
     */
    public TaskId {
        Objects.requireNonNull(value, "Task ID must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("Task ID must not be blank");
        }
    }
}

