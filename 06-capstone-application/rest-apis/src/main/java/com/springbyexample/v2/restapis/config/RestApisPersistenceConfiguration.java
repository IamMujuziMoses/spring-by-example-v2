package com.springbyexample.v2.restapis.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.springbyexample.v2.springdataandpersistence.persistence.entity.ProjectEntity;
import com.springbyexample.v2.springdataandpersistence.persistence.entity.TaskEntity;

/**
 * Configures persistence components used by the REST APIs application.
 *
 * @author Mujuzi Moses
 */
@Configuration
@EntityScan(basePackageClasses = { ProjectEntity.class, TaskEntity.class } )
@EnableJpaRepositories(basePackages = "com.springbyexample.v2.springdataandpersistence.persistence.repository")
public class RestApisPersistenceConfiguration {
}

