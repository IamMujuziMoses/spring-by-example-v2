package com.springbyexample.v2.applicationdesignandarchitecture.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.applicationdesignandarchitecture.application.exception.ProjectNotFoundException;
import com.springbyexample.v2.applicationdesignandarchitecture.application.exception.TaskNotFoundException;
import com.springbyexample.v2.applicationdesignandarchitecture.application.repository.ProjectRepository;
import com.springbyexample.v2.applicationdesignandarchitecture.application.repository.TaskRepository;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.Task;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskStatus;

/**
 * Unit tests for task application use cases.
 *
 * @author Mujuzi Moses
 */
public class TaskApplicationServiceTest {

    @Test
    void shouldCreateTaskForExistingProject() {
        InMemoryProjectRepository projects = new InMemoryProjectRepository();
        InMemoryTaskRepository tasks = new InMemoryTaskRepository();

        ProjectId projectId = new ProjectId("project-1");
        projects.save(new Project(projectId, "Capstone", "Description"));

        TaskApplicationService service = new TaskApplicationService(projects, tasks);

        Task task = service.createTask(
                new TaskId("task-1"), projectId, "Design architecture", "Define application layers");

        assertEquals(TaskStatus.TODO, task.status());
        assertEquals(projectId, task.projectId());
        assertEquals(task, tasks.findById(task.id()).orElseThrow());
    }

    @Test
    void shouldRejectTaskForMissingProject() {
        TaskApplicationService service =
                new TaskApplicationService(new InMemoryProjectRepository(), new InMemoryTaskRepository());

        assertThrows(ProjectNotFoundException.class, () -> service.createTask(
                        new TaskId("task-1"), new ProjectId("missing"), "Task", "Description"));
    }

    @Test
    void shouldTransitionAndSaveTask() {
        InMemoryProjectRepository projects = new InMemoryProjectRepository();
        InMemoryTaskRepository tasks = new InMemoryTaskRepository();

        ProjectId projectId = new ProjectId("project-1");
        TaskId taskId = new TaskId("task-1");

        projects.save(new Project(projectId, "Capstone", "Description"));
        tasks.save(new Task(
                taskId, projectId, "Design architecture", "Define application layers", TaskStatus.TODO));

        TaskApplicationService service = new TaskApplicationService(projects, tasks);

        Task updated = service.transitionTask(taskId, TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, updated.status());
        assertEquals(TaskStatus.IN_PROGRESS, tasks.findById(taskId).orElseThrow().status());
    }

    @Test
    void shouldReturnTasksForProject() {
        InMemoryProjectRepository projects = new InMemoryProjectRepository();
        InMemoryTaskRepository tasks = new InMemoryTaskRepository();

        ProjectId projectId = new ProjectId("project-1");
        projects.save(new Project(projectId, "Capstone", "Description"));

        tasks.save(new Task(
                new TaskId("task-1"), projectId, "First task", "First description", TaskStatus.TODO));
        tasks.save(new Task(
                new TaskId("task-2"), projectId, "Second task", "Second description", TaskStatus.TODO));

        TaskApplicationService service = new TaskApplicationService(projects, tasks);

        List<Task> result = service.getTasksForProject(projectId);

        assertEquals(2, result.size());
    }

    @Test
    void shouldThrowWhenTaskDoesNotExist() {
        TaskApplicationService service = new TaskApplicationService(
                new InMemoryProjectRepository(), new InMemoryTaskRepository());

        assertThrows(TaskNotFoundException.class, () -> service.getTask(new TaskId("missing")));
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

    private static class InMemoryTaskRepository implements TaskRepository {

        private final Map<TaskId, Task> tasks = new HashMap<>();

        @Override
        public Task save(Task task) {
            tasks.put(task.id(), task);
            return task;
        }

        @Override
        public Optional<Task> findById(TaskId id) {
            return Optional.ofNullable(tasks.get(id));
        }

        @Override
        public List<Task> findByProjectId(ProjectId projectId) {
            List<Task> matchingTasks = new ArrayList<>();

            for (Task task : tasks.values()) {
                if (task.projectId().equals(projectId)) {
                    matchingTasks.add(task);
                }
            }

            return List.copyOf(matchingTasks);
        }
    }
}

