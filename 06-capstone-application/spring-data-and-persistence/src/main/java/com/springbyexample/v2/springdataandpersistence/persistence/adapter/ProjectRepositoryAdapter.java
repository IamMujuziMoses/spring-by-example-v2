package com.springbyexample.v2.springdataandpersistence.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.springbyexample.v2.applicationdesignandarchitecture.application.repository.ProjectRepository;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.springdataandpersistence.persistence.mapper.ProjectPersistenceMapper;
import com.springbyexample.v2.springdataandpersistence.persistence.repository.ProjectJpaRepository;

/**
 * JPA-backed implementation of the project repository.
 *
 * @author Mujuzi Moses
 */
@Repository
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final ProjectJpaRepository repository;

    private final ProjectPersistenceMapper mapper;

    /**
     * Creates the project repository adapter.
     *
     * @param repository the Spring Data repository
     * @param mapper the project persistence mapper
     */
    public ProjectRepositoryAdapter(ProjectJpaRepository repository, ProjectPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Project save(Project project) {
        return mapper.toDomain(repository.save(mapper.toEntity(project)));
    }

    @Override
    public Optional<Project> findById(ProjectId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }
}
