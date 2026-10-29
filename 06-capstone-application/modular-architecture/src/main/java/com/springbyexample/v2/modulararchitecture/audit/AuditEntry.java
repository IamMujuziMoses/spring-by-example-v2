package com.springbyexample.v2.modulararchitecture.audit;

import java.time.Instant;

/**
 * Represents an entry in the application audit log.
 *
 * @param eventType   type of event recorded
 * @param description human-readable event description
 * @param taskId      related task identifier
 * @param projectId   related project identifier
 * @param occurredAt  time the event occurred
 * @author Mujuzi Moses
 */
public record AuditEntry(String eventType, String description, Long taskId, Long projectId, Instant occurredAt) {
}
