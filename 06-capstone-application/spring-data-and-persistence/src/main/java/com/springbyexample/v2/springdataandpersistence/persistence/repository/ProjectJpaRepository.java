package com.springbyexample.v2.springdataandpersistence.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springbyexample.v2.springdataandpersistence.persistence.entity.ProjectEntity;

/**
 * Spring Data repository for project entities.
 *
 * @author Mujuzi Moses
 */
public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, String> {
}