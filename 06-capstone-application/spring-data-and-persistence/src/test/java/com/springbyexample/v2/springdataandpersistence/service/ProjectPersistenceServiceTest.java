package com.springbyexample.v2.springdataandpersistence.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;

/**
 * Integration tests for the project persistence service.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class ProjectPersistenceServiceTest {

    @Autowired
    private ProjectPersistenceService projectService;

    @Test
    void shouldCreateAndRetrieveProject() {
        ProjectId id = new ProjectId("service-project");

        Project created = projectService.createProject(
                id, "Service integration", "Project created through the persistence service");

        Project retrieved = projectService.getProject(id);

        assertEquals(created, retrieved);
        assertEquals("Service integration", retrieved.name());
    }
}
