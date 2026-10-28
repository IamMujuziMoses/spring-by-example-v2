package com.springbyexample.v2.restapis.api.dto;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskStatus;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for transitioning a task to another status.
 *
 * @author Mujuzi Moses
 */
public record UpdateTaskStatusRequest(@NotNull TaskStatus status) {
}
