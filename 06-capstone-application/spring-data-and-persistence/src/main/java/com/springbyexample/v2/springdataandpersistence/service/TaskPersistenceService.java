package com.springbyexample.v2.springdataandpersistence.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springbyexample.v2.applicationdesignandarchitecture.application.service.TaskApplicationService;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.Task;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskStatus;

/**
 * Provides transactional task persistence operations.
 *
 * @author Mujuzi Moses
 */
@Service
public class TaskPersistenceService {

    private final TaskApplicationService taskService;

    /**
     * Creates the task persistence service.
     *
     * @param taskService the task application service
     */
    public TaskPersistenceService(TaskApplicationService taskService) {
        this.taskService = taskService;
    }

    /**
     * Creates and persists a task.
     *
     * @param id          the task identifier
     * @param projectId   the owning project identifier
     * @param title       the task title
     * @param description the task description
     * @return the saved task
     */
    @Transactional
    public Task createTask(TaskId id, ProjectId projectId, String title, String description) {
        return taskService.createTask(id, projectId, title, description);
    }

    /**
     * Retrieves a task.
     *
     * @param id the task identifier
     * @return the requested task
     */
    @Transactional(readOnly = true)
    public Task getTask(TaskId id) {
        return taskService.getTask(id);
    }

    /**
     * Retrieves all tasks belonging to a project.
     *
     * @param projectId the owning project identifier
     * @return the project's tasks
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksForProject(ProjectId projectId) {
        return taskService.getTasksForProject(projectId);
    }

    /**
     * Changes and persists a task's status.
     *
     * @param id        the task identifier
     * @param newStatus the requested status
     * @return the updated task
     */
    @Transactional
    public Task transitionTask(TaskId id, TaskStatus newStatus) {
        return taskService.transitionTask(id, newStatus);
    }
}
