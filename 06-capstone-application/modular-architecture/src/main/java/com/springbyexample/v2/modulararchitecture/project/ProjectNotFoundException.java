package com.springbyexample.v2.modulararchitecture.project;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Indicates that a requested project does not exist.
 *
 * @author Mujuzi Moses
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(Long projectId) {
        super("Project not found with id: " + projectId);
    }
}
