package com.springbyexample.v2.modulararchitecture.project;

/**
 * Represents a project managed by the application.
 *
 * @param id          project identifier
 * @param name        project name
 * @param description project description
 * @author Mujuzi Moses
 */
public record Project(Long id, String name, String description) {
}
