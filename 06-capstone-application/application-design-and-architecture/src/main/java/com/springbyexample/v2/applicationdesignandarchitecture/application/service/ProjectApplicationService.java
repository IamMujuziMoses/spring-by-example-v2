package com.springbyexample.v2.applicationdesignandarchitecture.application.service;

import java.util.Objects;

import com.springbyexample.v2.applicationdesignandarchitecture.application.exception.ProjectNotFoundException;
import com.springbyexample.v2.applicationdesignandarchitecture.application.repository.ProjectRepository;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;

/**
 * Coordinates project-related application use cases.
 *
 * @author Mujuzi Moses
 */
public class ProjectApplicationService {

    private final ProjectRepository projectRepository;

    /**
     * Creates the project application service.
     *
     * @param projectRepository the project repository
     */
    public ProjectApplicationService(ProjectRepository projectRepository) {
        this.projectRepository = Objects.requireNonNull(projectRepository, "Project repository must not be null");
    }

    /**
     * Creates and saves a project.
     *
     * @param id the project identifier
     * @param name the project name
     * @param description the project description
     * @return the saved project
     */
    public Project createProject(ProjectId id, String name, String description) {
        Project project = new Project(id, name, description);
        return projectRepository.save(project);
    }

    /**
     * Retrieves a project by its identifier.
     *
     * @param id the project identifier
     * @return the requested project
     * @throws ProjectNotFoundException if the project does not exist
     */
    public Project getProject(ProjectId id) {
        Objects.requireNonNull(id, "Project ID must not be null");

        return projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
    }
}

