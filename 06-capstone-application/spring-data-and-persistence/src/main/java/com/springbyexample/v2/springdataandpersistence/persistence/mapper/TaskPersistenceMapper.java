package com.springbyexample.v2.springdataandpersistence.persistence.mapper;

import org.springframework.stereotype.Component;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.Task;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskId;
import com.springbyexample.v2.springdataandpersistence.persistence.entity.ProjectEntity;
import com.springbyexample.v2.springdataandpersistence.persistence.entity.TaskEntity;

/**
 * Maps between task domain objects and JPA entities.
 *
 * @author Mujuzi Moses
 */
@Component
public class TaskPersistenceMapper {

    /**
     * Converts a domain task to a JPA entity.
     *
     * @param task    the domain task
     * @param project the persisted project associated with the task
     * @return the corresponding entity
     */
    public TaskEntity toEntity(Task task, ProjectEntity project) {
        return new TaskEntity(task.id().value(), project, task.title(), task.description(), task.status());
    }

    /**
     * Converts a JPA entity to a domain task.
     *
     * @param entity the task entity
     * @return the corresponding domain task
     */
    public Task toDomain(TaskEntity entity) {
        return new Task(new TaskId(entity.getId()), new ProjectId(entity.getProject().getId()), entity.getTitle(),
                entity.getDescription(), entity.getStatus());
    }
}