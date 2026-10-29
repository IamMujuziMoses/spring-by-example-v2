package com.springbyexample.v2.transactionmanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springbyexample.v2.transactionmanagement.domain.Project;
import com.springbyexample.v2.transactionmanagement.domain.Task;
import com.springbyexample.v2.transactionmanagement.repository.ProjectRepository;
import com.springbyexample.v2.transactionmanagement.repository.TaskRepository;

/**
 * Coordinates atomic creation of projects and their tasks.
 *
 * @author Mujuzi Moses
 */
@Service
public class ProjectTransactionService {

    private final ProjectRepository projectRepository;

    private final TaskRepository taskRepository;

    private final TransactionAuditService auditService;

    public ProjectTransactionService(ProjectRepository projectRepository, TaskRepository taskRepository,
            TransactionAuditService auditService) {

        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.auditService = auditService;
    }

    /**
     * Creates a project and its tasks as one atomic operation.
     *
     * @param name        the project name
     * @param description the project description
     * @param taskTitles  the titles of the tasks to create
     * @return the persisted project
     * @throws IllegalArgumentException if the project name or a task title is blank
     */
    @Transactional
    public Project createProjectWithTasks(String name, String description, List<String> taskTitles) {

        try {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Project name must not be blank");
            }

            Project project = projectRepository.save(new Project(name, description));

            for (String title : taskTitles) {
                if (title == null || title.isBlank()) {
                    throw new IllegalArgumentException("Task title must not be blank");
                }

                taskRepository.save(new Task(title, project));
            }

            return project;

        } catch (RuntimeException exception) {
            auditService.recordProjectCreationFailure(name, exception.getMessage());

            throw exception;
        }
    }
}

