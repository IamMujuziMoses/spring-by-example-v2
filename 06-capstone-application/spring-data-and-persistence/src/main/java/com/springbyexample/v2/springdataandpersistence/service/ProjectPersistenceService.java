package com.springbyexample.v2.springdataandpersistence.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springbyexample.v2.applicationdesignandarchitecture.application.service.ProjectApplicationService;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;

/**
 * Provides transactional project persistence operations.
 *
 * @author Mujuzi Moses
 */
@Service
public class ProjectPersistenceService {

    private final ProjectApplicationService projectService;

    /**
     * Creates the project persistence service.
     *
     * @param projectService the project application service
     */
    public ProjectPersistenceService(ProjectApplicationService projectService) {
        this.projectService = projectService;
    }

    /**
     * Creates and persists a project.
     *
     * @param id          the project identifier
     * @param name        the project name
     * @param description the project description
     * @return the saved project
     */
    @Transactional
    public Project createProject(ProjectId id, String name, String description) {
        return projectService.createProject(id, name, description);
    }

    /**
     * Retrieves a project.
     *
     * @param id the project identifier
     * @return the requested project
     */
    @Transactional(readOnly = true)
    public Project getProject(ProjectId id) {
        return projectService.getProject(id);
    }
}
