package com.springbyexample.v2.restapis.api.dto;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;

/**
 * Response representation of a project.
 *
 * @author Mujuzi Moses
 */
public record ProjectResponse(String id, String name, String description) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(project.id().value(), project.name(), project.description());
    }
}
