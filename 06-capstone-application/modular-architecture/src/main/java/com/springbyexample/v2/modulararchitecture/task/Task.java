package com.springbyexample.v2.modulararchitecture.task;

/**
 * Represents a task associated with a project.
 *
 * @param id        task identifier
 * @param projectId associated project identifier
 * @param title     task title
 * @param completed whether the task is completed
 * @author Mujuzi Moses
 */
public record Task(Long id, Long projectId, String title, boolean completed) {
}
