package com.springbyexample.v2.modulararchitecture.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.springbyexample.v2.modulararchitecture.project.Project;
import com.springbyexample.v2.modulararchitecture.project.ProjectNotFoundException;
import com.springbyexample.v2.modulararchitecture.project.ProjectService;

/**
 * Tests task management operations.
 *
 * @author Mujuzi Moses
 */
public class TaskServiceTest {

    private final ProjectService projectService = new ProjectService();

    private final List<TaskCreatedEvent> publishedEvents = new ArrayList<>();

    private final ApplicationEventPublisher eventPublisher = event -> publishedEvents.add((TaskCreatedEvent) event);

    private final TaskService taskService = new TaskService(projectService, eventPublisher);

    @Test
    void createTask_shouldCreateTaskAndPublishEvent() {
        Project project = projectService.createProject("Example project", "Project for task tests");

        Task task = taskService.createTask(project.id(), "Write tests");

        assertEquals(project.id(), task.projectId());
        assertEquals("Write tests", task.title());
        assertFalse(task.completed());

        assertEquals(1, publishedEvents.size());
        assertEquals(task.id(), publishedEvents.getFirst().taskId());
        assertEquals(project.id(), publishedEvents.getFirst().projectId());
        assertEquals("Write tests", publishedEvents.getFirst().title());
        assertNotNull(publishedEvents.getFirst().occurredAt());
    }

    @Test
    void createTask_shouldRejectUnknownProject() {
        assertThrows(ProjectNotFoundException.class, () -> taskService.createTask(99L, "Write tests"));

        assertTrue(publishedEvents.isEmpty());
        assertTrue(taskService.getTasks().isEmpty());
    }

    @Test
    void createTask_shouldRejectNullTitle() {
        Project project = projectService.createProject("Example project", "Project for task tests");

        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(project.id(), null));

        assertTrue(publishedEvents.isEmpty());
        assertTrue(taskService.getTasks().isEmpty());
    }

    @Test
    void createTask_shouldRejectBlankTitle() {
        Project project = projectService.createProject("Example project", "Project for task tests");

        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(project.id(), "  "));

        assertTrue(publishedEvents.isEmpty());
        assertTrue(taskService.getTasks().isEmpty());
    }

    @Test
    void createTask_shouldTrimTaskTitle() {
        Project project = projectService.createProject("Example project", "Project for task tests");

        Task task = taskService.createTask(project.id(), "  Write tests  ");

        assertEquals("Write tests", task.title());
        assertEquals("Write tests", publishedEvents.getFirst().title());
    }

    @Test
    void createTask_shouldAssignUniqueIds() {
        Project project = projectService.createProject("Example project", "Project for task tests");

        Task firstTask = taskService.createTask(project.id(), "First task");
        Task secondTask = taskService.createTask(project.id(), "Second task");

        assertNotEquals(firstTask.id(), secondTask.id());
        assertEquals(2, publishedEvents.size());
    }

    @Test
    void getTasks_shouldReturnEmptyListWhenNoTasksExist() {
        assertTrue(taskService.getTasks().isEmpty());
    }

    @Test
    void getTasksForProject_shouldReturnOnlyTasksForRequestedProject() {
        Project firstProject = projectService.createProject("First project", "First project description");
        Project secondProject = projectService.createProject("Second project", "Second project description");

        taskService.createTask(firstProject.id(), "First task");
        taskService.createTask(secondProject.id(), "Second task");
        taskService.createTask(firstProject.id(), "Third task");

        List<Task> tasks = taskService.getTasksForProject(firstProject.id());

        assertEquals(2, tasks.size());
        assertEquals("First task", tasks.get(0).title());
        assertEquals("Third task", tasks.get(1).title());
        assertTrue(tasks.stream().allMatch(task -> task.projectId().equals(firstProject.id())));
    }

    @Test
    void getTasksForProject_shouldReturnEmptyListWhenProjectHasNoTasks() {
        Project project = projectService.createProject("Example project", "Project without tasks");

        assertTrue(taskService.getTasksForProject(project.id()).isEmpty());
    }

    @Test
    void getTask_shouldReturnRequestedTask() {
        Project project = projectService.createProject("Example project", "Project for task tests");
        Task task = taskService.createTask(project.id(), "Write tests");

        assertEquals(task, taskService.getTask(task.id()));
    }

    @Test
    void getTask_shouldThrowWhenTaskDoesNotExist() {
        assertThrows(TaskNotFoundException.class, () -> taskService.getTask(99L));
    }
}