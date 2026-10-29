package com.springbyexample.v2.modulararchitecture.project;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

/**
 * Provides project management operations.
 *
 * @author Mujuzi Moses
 */
@Service
public class ProjectService {

    private final Map<Long, Project> projects = new ConcurrentHashMap<>();

    private final AtomicLong sequence = new AtomicLong();

    public Project createProject(String name, String description) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name must not be blank.");
        }

        Long id = sequence.incrementAndGet();
        Project project = new Project(id, name.trim(), description);
        projects.put(id, project);

        return project;
    }

    public Project getProject(Long projectId) {
        Project project = projects.get(projectId);

        if (project == null) {
            throw new ProjectNotFoundException(projectId);
        }

        return project;
    }

    public List<Project> getProjects() {

        return projects.values().stream().sorted(Comparator.comparing(Project::id)).toList();
    }
}
