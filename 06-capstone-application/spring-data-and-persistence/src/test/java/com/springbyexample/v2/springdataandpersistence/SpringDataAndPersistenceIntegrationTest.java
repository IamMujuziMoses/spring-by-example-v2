package com.springbyexample.v2.springdataandpersistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.applicationdesignandarchitecture.application.service.ProjectApplicationService;
import com.springbyexample.v2.applicationdesignandarchitecture.application.service.TaskApplicationService;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.Task;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskStatus;

/**
 * Integration tests for Spring Data and persistence.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class SpringDataAndPersistenceIntegrationTest {

    @Autowired
    private ProjectApplicationService projectService;

    @Autowired
    private TaskApplicationService taskService;

    @Test
    void shouldPersistAndRetrieveProjectAndTask() {
        ProjectId projectId = new ProjectId("project-1");
        TaskId taskId = new TaskId("task-1");

        Project savedProject = projectService.createProject(
                projectId, "Capstone", "Capstone application project");

        Task savedTask = taskService.createTask(taskId,
                projectId, "Implement persistence", "Persist projects and tasks using Spring Data JPA");

        Project retrievedProject = projectService.getProject(projectId);
        Task retrievedTask = taskService.getTask(taskId);
        List<Task> projectTasks = taskService.getTasksForProject(projectId);

        assertNotNull(savedProject);
        assertEquals(projectId, retrievedProject.id());
        assertEquals("Capstone", retrievedProject.name());

        assertNotNull(savedTask);
        assertEquals(taskId, retrievedTask.id());
        assertEquals(projectId, retrievedTask.projectId());
        assertEquals(TaskStatus.TODO, retrievedTask.status());

        assertEquals(1, projectTasks.size());
        assertEquals(taskId, projectTasks.get(0).id());
    }

    @Test
    void shouldPersistTaskStatusTransitions() {
        ProjectId projectId = new ProjectId("transition-project");
        TaskId taskId = new TaskId("transition-task");

        projectService.createProject(projectId, "Transitions", "Task transition testing");

        taskService.createTask(
                taskId, projectId, "Complete implementation", "Verify status persistence");

        taskService.transitionTask(taskId, TaskStatus.IN_PROGRESS);
        Task inProgressTask = taskService.getTask(taskId);

        assertEquals(TaskStatus.IN_PROGRESS, inProgressTask.status());

        taskService.transitionTask(taskId, TaskStatus.COMPLETED);
        Task completedTask = taskService.getTask(taskId);

        assertEquals(TaskStatus.COMPLETED, completedTask.status());
    }
}
