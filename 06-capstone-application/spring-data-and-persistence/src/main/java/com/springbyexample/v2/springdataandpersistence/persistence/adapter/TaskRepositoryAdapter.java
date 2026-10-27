package com.springbyexample.v2.springdataandpersistence.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.springbyexample.v2.applicationdesignandarchitecture.application.repository.TaskRepository;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.Task;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskId;
import com.springbyexample.v2.springdataandpersistence.persistence.mapper.TaskPersistenceMapper;
import com.springbyexample.v2.springdataandpersistence.persistence.repository.ProjectJpaRepository;
import com.springbyexample.v2.springdataandpersistence.persistence.repository.TaskJpaRepository;

/**
 * JPA-backed implementation of the task repository.
 *
 * @author Mujuzi Moses
 */
@Repository
public class TaskRepositoryAdapter implements TaskRepository {

    private final TaskJpaRepository taskRepository;

    private final ProjectJpaRepository projectRepository;

    private final TaskPersistenceMapper mapper;

    /**
     * Creates the task repository adapter.
     *
     * @param taskRepository the Spring Data task repository
     * @param projectRepository the Spring Data project repository
     * @param mapper the task persistence mapper
     */
    public TaskRepositoryAdapter(TaskJpaRepository taskRepository, ProjectJpaRepository projectRepository,
            TaskPersistenceMapper mapper) {

        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.mapper = mapper;
    }

    @Override
    public Task save(Task task) {
        var project = projectRepository.findById(task.projectId().value())
                .orElseThrow(() -> new IllegalStateException("Project does not exist: " + task.projectId().value()));

        return mapper.toDomain(taskRepository.save(mapper.toEntity(task, project)));
    }

    @Override
    public Optional<Task> findById(TaskId id) {
        return taskRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Task> findByProjectId(ProjectId projectId) {
        return taskRepository.findAllByProject_Id(projectId.value())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}