package com.springbyexample.v2.restapis.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a project.
 *
 * @author Mujuzi Moses
 */
public record CreateProjectRequest(@NotBlank String id, @NotBlank @Size(max = 255) String name,
        @Size(max = 2000) String description) {
}
