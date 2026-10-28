package com.springbyexample.v2.restapis.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.restapis.api.dto.CreateProjectRequest;
import com.springbyexample.v2.restapis.api.dto.ProjectResponse;
import com.springbyexample.v2.springdataandpersistence.service.ProjectPersistenceService;

import jakarta.validation.Valid;

/**
 * REST endpoints for project operations.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectPersistenceService projectService;

    public ProjectController(ProjectPersistenceService projectService) {
        this.projectService = projectService;
    }

    /**
     * Creates a project.
     *
     * @param request project creation details
     * @return the created project
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        Project project = projectService.createProject(new ProjectId(request.id()), request.name(), request.description());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(project.id().value())
                .toUri();

        return ResponseEntity.created(location).body(ProjectResponse.from(project));
    }

    /**
     * Retrieves a project by its identifier.
     *
     * @param projectId project identifier
     * @return the requested project
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable String projectId) {
        Project project = projectService.getProject(new ProjectId(projectId));

        return ResponseEntity.ok(ProjectResponse.from(project));
    }
}
