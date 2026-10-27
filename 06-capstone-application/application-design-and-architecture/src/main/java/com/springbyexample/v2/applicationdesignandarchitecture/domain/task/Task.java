package com.springbyexample.v2.applicationdesignandarchitecture.domain.task;

import java.util.Objects;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.exception.TaskTransitionException;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;

/**
 * Represents a task belonging to a project.
 *
 * @param id the task identifier
 * @param projectId the identifier of the owning project
 * @param title the task title
 * @param description the task description
 * @param status the current task status
 * @author Mujuzi Moses
 */
public record Task(TaskId id, ProjectId projectId, String title, String description, TaskStatus status) {

    /**
     * Validates the task state.
     */
    public Task {
        Objects.requireNonNull(id, "Task ID must not be null");
        Objects.requireNonNull(projectId, "Project ID must not be null");
        Objects.requireNonNull(title, "Task title must not be null");
        Objects.requireNonNull(status, "Task status must not be null");

        if (title.isBlank()) {
            throw new IllegalArgumentException("Task title must not be blank");
        }
    }

    /**
     * Transitions this task to the requested status.
     *
     * @param newStatus the requested status
     * @return a new task with the updated status
     * @throws NullPointerException if the requested status is null
     * @throws TaskTransitionException if the transition is not allowed
     */
    public Task transitionTo(TaskStatus newStatus) {
        Objects.requireNonNull(newStatus, "New task status must not be null");

        boolean allowed = switch (status) {
            case TODO -> newStatus == TaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> newStatus == TaskStatus.COMPLETED;
            case COMPLETED -> false;
        };

        if (!allowed) {
            throw new TaskTransitionException("Cannot transition task from " + status + " to " + newStatus);
        }

        return new Task(id, projectId, title, description, newStatus);
    }
}

