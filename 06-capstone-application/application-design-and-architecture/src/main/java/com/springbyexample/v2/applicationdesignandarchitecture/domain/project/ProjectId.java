package com.springbyexample.v2.applicationdesignandarchitecture.domain.project;

import java.util.Objects;

/**
 * Represents the unique identifier of a project.
 *
 * @author Mujuzi Moses
 */
public record ProjectId(String value) {

    /**
     * Creates a project identifier.
     *
     * @param value the identifier value
     */
    public ProjectId {
        Objects.requireNonNull(value, "Project ID must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("Project ID must not be blank");
        }
    }
}
