package com.springbyexample.v2.applicationdesignandarchitecture.application.repository;

import java.util.List;
import java.util.Optional;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.Task;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskId;

/**
 * Defines persistence operations for tasks.
 *
 * @author Mujuzi Moses
 */
public interface TaskRepository {

    /**
     * Saves a task.
     *
     * @param task the task to save
     * @return the saved task
     */
    Task save(Task task);

    /**
     * Finds a task by its identifier.
     *
     * @param id the task identifier
     * @return the task, if found
     */
    Optional<Task> findById(TaskId id);

    /**
     * Finds all tasks belonging to a project.
     *
     * @param projectId the owning project identifier
     * @return tasks belonging to the project
     */
    List<Task> findByProjectId(ProjectId projectId);
}

