package com.springbyexample.v2.transactionmanagement.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Records transaction-related events independently of project persistence.
 *
 * @author Mujuzi Moses
 */
@Entity
@Table(name = "transaction_audits")
public class TransactionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType;

    @Column
    private String projectName;

    @Column(length = 2000)
    private String details;

    @Column(nullable = false)
    private Instant occurredAt;

    protected TransactionAudit() {
    }

    public TransactionAudit(String eventType, String projectName, String details, Instant occurredAt) {
        this.eventType = eventType;
        this.projectName = projectName;
        this.details = details;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getDetails() {
        return details;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
