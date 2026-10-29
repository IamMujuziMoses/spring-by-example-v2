package com.springbyexample.v2.modulararchitecture.task;

import java.time.Instant;

/**
 * Published when a task is created.
 *
 * @param taskId     created task identifier
 * @param projectId  associated project identifier
 * @param title      created task title
 * @param occurredAt event creation time
 * @author Mujuzi Moses
 */
public record TaskCreatedEvent(Long taskId, Long projectId, String title, Instant occurredAt) {
}
