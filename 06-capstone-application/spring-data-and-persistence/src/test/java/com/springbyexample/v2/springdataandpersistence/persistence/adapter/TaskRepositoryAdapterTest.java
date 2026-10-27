package com.springbyexample.v2.springdataandpersistence.persistence.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.Task;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskStatus;
import com.springbyexample.v2.springdataandpersistence.persistence.entity.ProjectEntity;
import com.springbyexample.v2.springdataandpersistence.persistence.mapper.TaskPersistenceMapper;
import com.springbyexample.v2.springdataandpersistence.persistence.repository.ProjectJpaRepository;
import com.springbyexample.v2.springdataandpersistence.persistence.repository.TaskJpaRepository;

/**
 * Integration tests for task persistence.
 *
 * @author Mujuzi Moses
 */
@DataJpaTest
@Import({TaskRepositoryAdapter.class, TaskPersistenceMapper.class})
public class TaskRepositoryAdapterTest {

    @Autowired
    private TaskRepositoryAdapter adapter;

    @Autowired
    private ProjectJpaRepository projectRepository;

    @Autowired
    private TaskJpaRepository taskRepository;

    private ProjectId projectId;

    @BeforeEach
    void setUp() {
        projectId = new ProjectId("task-test-project");

        projectRepository.save(new ProjectEntity(projectId.value(), "Task test project",
                "Project used by task persistence tests"));
    }

    @Test
    void shouldSaveAndRetrieveTask() {
        Task task = new Task(new TaskId("adapter-task"),
                projectId, "Test persistence", "Verify task mapping", TaskStatus.TODO);

        Task saved = adapter.save(task);

        assertEquals(task, saved);
        assertEquals(task, adapter.findById(task.id()).orElseThrow());
    }

    @Test
    void shouldFindTasksByProject() {
        Task first = new Task(new TaskId("first-task"),
                projectId, "First task", "First task description", TaskStatus.TODO);

        Task second = new Task(new TaskId("second-task"),
                projectId, "Second task", "Second task description", TaskStatus.IN_PROGRESS);

        adapter.save(first);
        adapter.save(second);

        assertEquals(2, adapter.findByProjectId(projectId).size());
    }

    @Test
    void shouldReturnEmptyWhenTaskDoesNotExist() {
        assertTrue(adapter.findById(new TaskId("missing-task")).isEmpty());
    }
}