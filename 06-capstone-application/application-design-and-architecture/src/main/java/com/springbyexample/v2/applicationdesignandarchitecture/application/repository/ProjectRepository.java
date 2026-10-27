package com.springbyexample.v2.applicationdesignandarchitecture.application.repository;

import java.util.Optional;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;

/**
 * Defines persistence operations for projects.
 *
 * @author Mujuzi Moses
 */
public interface ProjectRepository {

    /**
     * Saves a project.
     *
     * @param project the project to save
     * @return the saved project
     */
    Project save(Project project);

    /**
     * Finds a project by its identifier.
     *
     * @param id the project identifier
     * @return the project, if found
     */
    Optional<Project> findById(ProjectId id);
}

