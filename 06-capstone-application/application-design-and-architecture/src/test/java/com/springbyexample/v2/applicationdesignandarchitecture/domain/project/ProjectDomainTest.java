package com.springbyexample.v2.applicationdesignandarchitecture.domain.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the project domain model.
 *
 * @author Mujuzi Moses
 */
public class ProjectDomainTest {

    @Test
    void shouldCreateProjectWithValidDetails() {
        Project project = new Project(
                new ProjectId("project-1"), "Capstone", "Spring capstone application");

        assertEquals("project-1", project.id().value());
        assertEquals("Capstone", project.name());
        assertEquals("Spring capstone application", project.description());
    }

    @Test
    void shouldRejectBlankProjectName() {
        assertThrows(IllegalArgumentException.class, () -> new Project(
                        new ProjectId("project-1"), " ", "Description"));
    }

    @Test
    void shouldRejectBlankProjectIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> new ProjectId(" "));
    }
}

