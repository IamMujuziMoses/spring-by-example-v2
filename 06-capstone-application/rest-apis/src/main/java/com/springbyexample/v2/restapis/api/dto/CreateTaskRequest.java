package com.springbyexample.v2.restapis.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a task.
 *
 * @author Mujuzi Moses
 */
public record CreateTaskRequest(@NotBlank String id, @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description) {
}
