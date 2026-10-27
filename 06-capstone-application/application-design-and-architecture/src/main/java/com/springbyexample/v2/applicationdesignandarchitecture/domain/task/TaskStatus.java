package com.springbyexample.v2.applicationdesignandarchitecture.domain.task;

/**
 * Represents the lifecycle states of a task.
 *
 * @author Mujuzi Moses
 */
public enum TaskStatus {

    /** A task that has not yet started. */
    TODO,

    /** A task currently being worked on. */
    IN_PROGRESS,

    /** A task that has been completed. */
    COMPLETED
}

