package com.springbyexample.v2.springdataandpersistence.persistence.entity;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA entity representing a persisted task.
 *
 * @author Mujuzi Moses
 */
@Entity
@Table(name = "tasks")
public class TaskEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    /**
     * Required by JPA.
     */
    protected TaskEntity() {
    }

    /**
     * Creates a task entity.
     *
     * @param id          the task identifier
     * @param project     the owning project
     * @param title       the task title
     * @param description the task description
     * @param status      the task status
     */
    public TaskEntity(String id, ProjectEntity project, String title, String description, TaskStatus status) {
        this.id = id;
        this.project = project;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }
}
