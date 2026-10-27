package com.springbyexample.v2.springdataandpersistence.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.springdataandpersistence.persistence.mapper.ProjectPersistenceMapper;
import com.springbyexample.v2.springdataandpersistence.persistence.repository.ProjectJpaRepository;

/**
 * Integration tests for project persistence.
 *
 * @author Mujuzi Moses
 */
@DataJpaTest
@Import({ProjectRepositoryAdapter.class, ProjectPersistenceMapper.class})
public class ProjectRepositoryAdapterTest {

    @Autowired
    private ProjectRepositoryAdapter adapter;

    @Autowired
    private ProjectJpaRepository jpaRepository;

    @Test
    void shouldSaveAndRetrieveProject() {
        Project project = new Project(new ProjectId("adapter-project"), "Persistence example",
                "Test project persistence");

        Project saved = adapter.save(project);

        assertEquals(project, saved);
        assertTrue(adapter.findById(project.id()).isPresent());
        assertEquals(1, jpaRepository.count());
    }

    @Test
    void shouldReturnEmptyWhenProjectDoesNotExist() {
        assertTrue(adapter.findById(new ProjectId("missing-project")).isEmpty());
    }
}
