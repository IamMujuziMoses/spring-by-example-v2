package com.springbyexample.v2.applicationdesignandarchitecture.application.service;

import java.util.List;
import java.util.Objects;

import com.springbyexample.v2.applicationdesignandarchitecture.application.exception.ProjectNotFoundException;
import com.springbyexample.v2.applicationdesignandarchitecture.application.exception.TaskNotFoundException;
import com.springbyexample.v2.applicationdesignandarchitecture.application.repository.ProjectRepository;
import com.springbyexample.v2.applicationdesignandarchitecture.application.repository.TaskRepository;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.exception.TaskTransitionException;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.Task;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskStatus;

/**
 * Coordinates task-related application use cases.
 *
 * @author Mujuzi Moses
 */
public class TaskApplicationService {

    private final ProjectRepository projectRepository;

    private final TaskRepository taskRepository;

    /**
     * Creates the task application service.
     *
     * @param projectRepository the project repository
     * @param taskRepository    the task repository
     */
    public TaskApplicationService(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = Objects.requireNonNull(projectRepository, "Project repository must not be null");

        this.taskRepository = Objects.requireNonNull(taskRepository, "Task repository must not be null");
    }

    /**
     * Creates a task for an existing project.
     *
     * @param id          the task identifier
     * @param projectId   the owning project identifier
     * @param title       the task title
     * @param description the task description
     * @return the saved task
     * @throws ProjectNotFoundException if the project does not exist
     */
    public Task createTask(TaskId id, ProjectId projectId, String title, String description) {
        Objects.requireNonNull(id, "Task ID must not be null");
        Objects.requireNonNull(projectId, "Project ID must not be null");

        projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));

        Task task = new Task(id, projectId, title, description, TaskStatus.TODO);

        return taskRepository.save(task);
    }

    /**
     * Retrieves a task by its identifier.
     *
     * @param id the task identifier
     * @return the requested task
     * @throws TaskNotFoundException if the task does not exist
     */
    public Task getTask(TaskId id) {
        Objects.requireNonNull(id, "Task ID must not be null");

        return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    /**
     * Retrieves all tasks belonging to a project.
     *
     * @param projectId the owning project identifier
     * @return the project's tasks
     * @throws ProjectNotFoundException if the project does not exist
     */
    public List<Task> getTasksForProject(ProjectId projectId) {
        Objects.requireNonNull(projectId, "Project ID must not be null");

        projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));

        return List.copyOf(taskRepository.findByProjectId(projectId));
    }

    /**
     * Transitions a task to a new status and saves the result.
     *
     * @param id        the task identifier
     * @param newStatus the requested status
     * @return the saved task
     * @throws TaskNotFoundException   if the task does not exist
     * @throws TaskTransitionException if the transition is not allowed
     */
    public Task transitionTask(TaskId id, TaskStatus newStatus) {
        Task task = getTask(id);
        Task updatedTask = task.transitionTo(newStatus);

        return taskRepository.save(updatedTask);
    }
}

