package com.springbyexample.v2.modulararchitecture.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests project management operations.
 *
 * @author Mujuzi Moses
 */
public class ProjectServiceTest {

    private final ProjectService projectService = new ProjectService();

    @Test
    void createProject_shouldStoreProject() {
        Project project = projectService.createProject("Spring Modulith",
                "Demonstrate modular architecture");

        assertEquals(1L, project.id());
        assertEquals("Spring Modulith", project.name());
        assertEquals(project, projectService.getProject(project.id()));
    }

    @Test
    void createProject_shouldTrimProjectName() {
        Project project = projectService.createProject("  Spring Modulith  ",
                "Demonstrate modular architecture");

        assertEquals("Spring Modulith", project.name());
    }

    @Test
    void createProject_shouldRejectNullName() {
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(null, "Description"));
    }

    @Test
    void createProject_shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject("  ", "Description"));
    }

    @Test
    void getProjects_shouldReturnEmptyListWhenNoProjectsExist() {
        assertEquals(List.of(), projectService.getProjects());
    }

    @Test
    void getProjects_shouldReturnProjectsInIdOrder() {
        projectService.createProject("First", "First project");
        projectService.createProject("Second", "Second project");

        List<Project> projects = projectService.getProjects();

        assertEquals(2, projects.size());
        assertEquals("First", projects.get(0).name());
        assertEquals("Second", projects.get(1).name());
    }

    @Test
    void getProject_shouldThrowWhenProjectDoesNotExist() {
        assertThrows(ProjectNotFoundException.class, () -> projectService.getProject(99L));
    }

    @Test
    void createProject_shouldAllowNullDescription() {
        Project project = projectService.createProject("Spring Modulith", null);

        assertEquals("Spring Modulith", project.name());
        assertNull(project.description());
    }
}