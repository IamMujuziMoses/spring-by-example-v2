package com.springbyexample.v2.applicationdesignandarchitecture.domain.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.exception.TaskTransitionException;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;

/**
 * Unit tests for task behavior and state transitions.
 *
 * @author Mujuzi Moses
 */
public class TaskDomainTest {

    @Test
    void shouldCreateTaskInTodoState() {
        Task task = createTask();

        assertEquals(TaskStatus.TODO, task.status());
    }

    @Test
    void shouldTransitionTaskFromTodoToInProgress() {
        Task task = createTask();

        Task updatedTask = task.transitionTo(TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.status());
        assertEquals(TaskStatus.TODO, task.status());
    }

    @Test
    void shouldTransitionTaskFromInProgressToCompleted() {
        Task task = createTask().transitionTo(TaskStatus.IN_PROGRESS);

        Task updatedTask = task.transitionTo(TaskStatus.COMPLETED);

        assertEquals(TaskStatus.COMPLETED, updatedTask.status());
    }

    @Test
    void shouldRejectTransitionFromTodoToCompleted() {
        Task task = createTask();

        assertThrows(TaskTransitionException.class, () -> task.transitionTo(TaskStatus.COMPLETED));
    }

    @Test
    void shouldRejectTransitionFromCompletedToTodo() {
        Task task = createTask().transitionTo(TaskStatus.IN_PROGRESS).transitionTo(TaskStatus.COMPLETED);

        assertThrows(TaskTransitionException.class, () -> task.transitionTo(TaskStatus.TODO));
    }

    @Test
    void shouldRejectBlankTaskTitle() {
        assertThrows(IllegalArgumentException.class, () -> new Task(
                new TaskId("task-1"), new ProjectId("project-1"), " ", "Description", TaskStatus.TODO));
    }

    @Test
    void shouldTransitionInProgressTaskToCompleted() {
        Task task = createTask().transitionTo(TaskStatus.IN_PROGRESS);

        Task completedTask = task.transitionTo(TaskStatus.COMPLETED);

        assertEquals(TaskStatus.COMPLETED, completedTask.status());
        assertEquals(TaskStatus.IN_PROGRESS, task.status());
    }

    private Task createTask() {
        return new Task(new TaskId("task-1"),
                new ProjectId("project-1"),
                "Design architecture",
                "Define the application layers",
                TaskStatus.TODO);
    }
}

