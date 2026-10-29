package com.springbyexample.v2.modulararchitecture.task;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.springbyexample.v2.modulararchitecture.project.ProjectService;

/**
 * Provides task management operations.
 *
 * @author Mujuzi Moses
 */
@Service
public class TaskService {

    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();

    private final AtomicLong sequence = new AtomicLong();

    private final ProjectService projectService;

    private final ApplicationEventPublisher eventPublisher;

    public TaskService(ProjectService projectService, ApplicationEventPublisher eventPublisher) {
        this.projectService = projectService;
        this.eventPublisher = eventPublisher;
    }

    public Task createTask(Long projectId, String title) {
        projectService.getProject(projectId);

        if (title == null || title.isBlank()) {

            throw new IllegalArgumentException("Task title must not be blank.");
        }

        Long id = sequence.incrementAndGet();
        Task task = new Task(id, projectId, title.trim(), false);
        tasks.put(id, task);

        eventPublisher.publishEvent(new TaskCreatedEvent(task.id(), task.projectId(), task.title(), Instant.now()));

        return task;
    }

    public Task getTask(Long taskId) {
        Task task = tasks.get(taskId);

        if (task == null) {
            throw new TaskNotFoundException(taskId);
        }

        return task;
    }

    public List<Task> getTasks() {

        return tasks.values().stream().sorted(Comparator.comparing(Task::id)).toList();
    }

    public List<Task> getTasksForProject(Long projectId) {
        projectService.getProject(projectId);

        return tasks.values().stream().filter(task -> task.projectId().equals(projectId))
                .sorted(Comparator.comparing(Task::id)).toList();
    }
}
