package com.springbyexample.v2.integrationbetweenspringtechnologies.audit;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents an audit record for an application event.
 *
 * @author Mujuzi Moses
 */
@Entity
@Table(name = "audit_entries")
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private Long aggregateId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Instant occurredAt;

    protected AuditEntry() {
    }

    public AuditEntry(String eventType, Long aggregateId, String description, Instant occurredAt) {
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.description = description;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getAggregateId() {
        return aggregateId;
    }

    public String getDescription() {
        return description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}