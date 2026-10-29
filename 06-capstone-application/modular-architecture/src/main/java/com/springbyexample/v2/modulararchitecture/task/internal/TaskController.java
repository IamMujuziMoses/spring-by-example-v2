package com.springbyexample.v2.modulararchitecture.task.internal;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.modulararchitecture.task.Task;
import com.springbyexample.v2.modulararchitecture.task.TaskService;

/**
 * Exposes REST endpoints for task management.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@RequestBody TaskRequest request) {
        return taskService.createTask(request.projectId(), request.title());
    }

    @GetMapping("/tasks")
    public List<Task> getTasks() {
        return taskService.getTasks();
    }

    @GetMapping("/tasks/{taskId}")
    public Task getTask(@PathVariable Long taskId) {
        return taskService.getTask(taskId);
    }

    @GetMapping("/projects/{projectId}/tasks")
    public List<Task> getTasksForProject(@PathVariable Long projectId) {
        return taskService.getTasksForProject(projectId);
    }

    /**
     * Request body for creating a task.
     *
     * @param projectId associated project identifier
     * @param title     task title
     */
    public record TaskRequest(Long projectId, String title) {
    }
}
