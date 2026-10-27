package com.springbyexample.v2.applicationdesignandarchitecture.domain.exception;

/**
 * Indicates that a task cannot transition to the requested state.
 *
 * @author Mujuzi Moses
 */
public class TaskTransitionException extends IllegalStateException {

    /**
     * Creates a task transition exception.
     *
     * @param message explanation of the invalid transition
     */
    public TaskTransitionException(String message) {
        super(message);
    }
}

