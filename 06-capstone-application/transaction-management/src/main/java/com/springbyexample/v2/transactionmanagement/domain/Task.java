package com.springbyexample.v2.transactionmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Represents a task belonging to a project.
 *
 * @author Mujuzi Moses
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    protected Task() {
    }

    public Task(String title, Project project) {
        this.title = title;
        this.project = project;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Project getProject() {
        return project;
    }
}

