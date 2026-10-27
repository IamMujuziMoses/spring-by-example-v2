package com.springbyexample.v2.springdataandpersistence.persistence.mapper;

import org.springframework.stereotype.Component;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.springdataandpersistence.persistence.entity.ProjectEntity;

/**
 * Maps between project domain objects and JPA entities.
 *
 * @author Mujuzi Moses
 */
@Component
public class ProjectPersistenceMapper {

    /**
     * Converts a domain project to a JPA entity.
     *
     * @param project the domain project
     * @return the corresponding entity
     */
    public ProjectEntity toEntity(Project project) {
        return new ProjectEntity(project.id().value(), project.name(), project.description());
    }

    /**
     * Converts a JPA entity to a domain project.
     *
     * @param entity the project entity
     * @return the corresponding domain project
     */
    public Project toDomain(ProjectEntity entity) {
        return new Project(new ProjectId(entity.getId()), entity.getName(), entity.getDescription());
    }
}