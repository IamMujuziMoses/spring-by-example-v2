package com.springbyexample.v2.springdataandpersistence.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.Task;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskStatus;

/**
 * Integration tests for the task persistence service.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class TaskPersistenceServiceTest {

    @Autowired
    private ProjectPersistenceService projectService;

    @Autowired
    private TaskPersistenceService taskService;

    @Test
    void shouldPersistTaskStatusTransitions() {
        ProjectId projectId = new ProjectId("service-task-project");
        TaskId taskId = new TaskId("service-task");

        projectService.createProject(
                projectId, "Task service integration", "Project used for task service tests");

        taskService.createTask(
                taskId, projectId, "Complete task", "Verify status transitions persist");

        Task inProgress = taskService.transitionTask(taskId, TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, inProgress.status());

        Task completed = taskService.transitionTask(taskId, TaskStatus.COMPLETED);
        assertEquals(TaskStatus.COMPLETED, completed.status());

        assertEquals(TaskStatus.COMPLETED, taskService.getTask(taskId).status());
        assertEquals(1, taskService.getTasksForProject(projectId).size());
    }
}
