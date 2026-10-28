package com.springbyexample.v2.restapis.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.Task;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskId;
import com.springbyexample.v2.restapis.api.dto.CreateTaskRequest;
import com.springbyexample.v2.restapis.api.dto.TaskResponse;
import com.springbyexample.v2.restapis.api.dto.UpdateTaskStatusRequest;
import com.springbyexample.v2.springdataandpersistence.service.ProjectPersistenceService;
import com.springbyexample.v2.springdataandpersistence.service.TaskPersistenceService;

import jakarta.validation.Valid;

/**
 * REST endpoints for task operations.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskPersistenceService taskService;

    private final ProjectPersistenceService projectService;

    public TaskController(TaskPersistenceService taskService, ProjectPersistenceService projectService) {
        this.taskService = taskService;
        this.projectService = projectService;
    }

    /**
     * Creates a task belonging to a project.
     *
     * @param projectId project identifier
     * @param request   task creation details
     * @return the created task
     */
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(@PathVariable String projectId,
            @Valid @RequestBody CreateTaskRequest request) {

        Task task = taskService.createTask(new TaskId(request.id()), new ProjectId(projectId), request.title(),
                request.description());

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/tasks/{taskId}")
                .buildAndExpand(task.id().value())
                .toUri();

        return ResponseEntity.created(location).body(TaskResponse.from(task));
    }

    /**
     * Retrieves a task by its identifier.
     *
     * @param taskId task identifier
     * @return the requested task
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable String taskId) {
        Task task = taskService.getTask(new TaskId(taskId));

        return ResponseEntity.ok(TaskResponse.from(task));
    }

    /**
     * Retrieves all tasks belonging to a project.
     *
     * @param projectId project identifier
     * @return tasks belonging to the project
     */
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskResponse>> getTasksForProject(@PathVariable String projectId) {
        ProjectId id = new ProjectId(projectId);

        // Ensure the project exists instead of returning an empty list for an unknown project.
        projectService.getProject(id);

        List<TaskResponse> tasks = taskService.getTasksForProject(id).stream().map(TaskResponse::from).toList();

        return ResponseEntity.ok(tasks);
    }

    /**
     * Transitions a task to a new status.
     *
     * @param taskId  task identifier
     * @param request requested status
     * @return the updated task
     */
    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<TaskResponse> transitionTask(@PathVariable String taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request) {

        Task task = taskService.transitionTask(new TaskId(taskId), request.status());

        return ResponseEntity.ok(TaskResponse.from(task));
    }
}
