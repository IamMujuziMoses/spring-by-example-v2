package com.springbyexample.v2.applicationdesignandarchitecture.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.applicationdesignandarchitecture.application.exception.ProjectNotFoundException;
import com.springbyexample.v2.applicationdesignandarchitecture.application.repository.ProjectRepository;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;

/**
 * Unit tests for project application use cases.
 *
 * @author Mujuzi Moses
 */
public class ProjectApplicationServiceTest {

    @Test
    void shouldCreateAndRetrieveProject() {
        ProjectRepository repository = new InMemoryProjectRepository();
        ProjectApplicationService service = new ProjectApplicationService(repository);

        Project created = service.createProject(
                new ProjectId("project-1"), "Capstone", "Application design example");
        Project retrieved = service.getProject(created.id());

        assertEquals(created, retrieved);
    }

    @Test
    void shouldThrowWhenProjectDoesNotExist() {
        ProjectApplicationService service = new ProjectApplicationService(new InMemoryProjectRepository());

        assertThrows(ProjectNotFoundException.class, () -> service.getProject(new ProjectId("missing")));
    }

    private static class InMemoryProjectRepository implements ProjectRepository {

        private final Map<ProjectId, Project> projects = new HashMap<>();

        @Override
        public Project save(Project project) {
            projects.put(project.id(), project);
            return project;
        }

        @Override
        public Optional<Project> findById(ProjectId id) {
            return Optional.ofNullable(projects.get(id));
        }
    }
}

