package com.springbyexample.v2.applicationdesignandarchitecture.domain.project;

import java.util.Objects;

/**
 * Represents a project in the task management application.
 *
 * @param id the project identifier
 * @param name the project name
 * @param description the project description
 * @author Mujuzi Moses
 */
public record Project(ProjectId id, String name, String description) {

    /**
     * Validates the project state.
     */
    public Project {
        Objects.requireNonNull(id, "Project ID must not be null");

        Objects.requireNonNull(name, "Project name must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Project name must not be blank");
        }
    }
}

