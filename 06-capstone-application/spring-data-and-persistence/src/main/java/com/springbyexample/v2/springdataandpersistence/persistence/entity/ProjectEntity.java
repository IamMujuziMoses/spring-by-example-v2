package com.springbyexample.v2.springdataandpersistence.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing a persisted project.
 *
 * @author Mujuzi Moses
 */
@Entity
@Table(name = "projects")
public class ProjectEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    /**
     * Required by JPA.
     */
    protected ProjectEntity() {
    }

    /**
     * Creates a project entity.
     *
     * @param id          the project identifier
     * @param name        the project name
     * @param description the project description
     */
    public ProjectEntity(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
