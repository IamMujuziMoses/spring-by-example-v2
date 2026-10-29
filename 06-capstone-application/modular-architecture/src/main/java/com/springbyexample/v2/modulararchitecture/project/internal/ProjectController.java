package com.springbyexample.v2.modulararchitecture.project.internal;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.modulararchitecture.project.Project;
import com.springbyexample.v2.modulararchitecture.project.ProjectService;

/**
 * Exposes REST endpoints for project management.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Project createProject(@RequestBody ProjectRequest request) {

        return projectService.createProject(request.name(), request.description());
    }

    @GetMapping
    public List<Project> getProjects() {
        return projectService.getProjects();
    }

    @GetMapping("/{projectId}")
    public Project getProject(@PathVariable Long projectId) {
        return projectService.getProject(projectId);
    }

    /**
     * Request body for creating a project.
     *
     * @param name        project name
     * @param description project description
     */
    public record ProjectRequest(String name, String description) {
    }
}
