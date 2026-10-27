package com.springbyexample.v2.applicationdesignandarchitecture.application.exception;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;

/**
 * Indicates that a requested project does not exist.
 *
 * @author Mujuzi Moses
 */
public class ProjectNotFoundException extends RuntimeException {

    /**
     * Creates an exception for a missing project.
     *
     * @param id the missing project identifier
     */
    public ProjectNotFoundException(ProjectId id) {
        super("Project not found: " + id.value());
    }
}

