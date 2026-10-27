package com.springbyexample.v2.springdataandpersistence.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.springbyexample.v2.applicationdesignandarchitecture.application.repository.ProjectRepository;
import com.springbyexample.v2.applicationdesignandarchitecture.application.repository.TaskRepository;
import com.springbyexample.v2.applicationdesignandarchitecture.application.service.ProjectApplicationService;
import com.springbyexample.v2.applicationdesignandarchitecture.application.service.TaskApplicationService;

/**
 * Configures the capstone application's application services.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class ApplicationServiceConfiguration {

    /**
     * Creates the project application service.
     *
     * @param projectRepository the project repository
     * @return the project application service
     */
    @Bean
    public ProjectApplicationService projectApplicationService(ProjectRepository projectRepository) {
        return new ProjectApplicationService(projectRepository);
    }

    /**
     * Creates the task application service.
     *
     * @param projectRepository the project repository
     * @param taskRepository    the task repository
     * @return the task application service
     */
    @Bean
    public TaskApplicationService taskApplicationService(ProjectRepository projectRepository,
            TaskRepository taskRepository) {

        return new TaskApplicationService(projectRepository, taskRepository);
    }
}
