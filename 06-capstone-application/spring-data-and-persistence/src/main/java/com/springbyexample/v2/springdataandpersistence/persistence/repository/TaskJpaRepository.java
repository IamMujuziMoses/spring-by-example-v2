package com.springbyexample.v2.springdataandpersistence.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springbyexample.v2.springdataandpersistence.persistence.entity.TaskEntity;

/**
 * Spring Data repository for task entities.
 *
 * @author Mujuzi Moses
 */
public interface TaskJpaRepository extends JpaRepository<TaskEntity, String> {

    /**
     * Finds tasks belonging to a project.
     *
     * @param projectId the project database identifier
     * @return the matching task entities
     */
    List<TaskEntity> findAllByProject_Id(String projectId);
}
