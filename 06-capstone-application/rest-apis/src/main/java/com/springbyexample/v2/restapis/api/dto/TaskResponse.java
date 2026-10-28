package com.springbyexample.v2.restapis.api.dto;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.Task;

/**
 * Response representation of a task.
 *
 * @author Mujuzi Moses
 */
public record TaskResponse(String id, String projectId, String title, String description, String status) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(task.id().value(), task.projectId().value(), task.title(), task.description(),
                task.status().name());
    }
}
