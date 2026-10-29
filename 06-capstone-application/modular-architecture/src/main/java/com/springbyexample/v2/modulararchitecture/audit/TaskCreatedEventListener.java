package com.springbyexample.v2.modulararchitecture.audit;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.modulararchitecture.task.TaskCreatedEvent;

/**
 * Records task creation events in the audit log.
 *
 * @author Mujuzi Moses
 */
@Component
public class TaskCreatedEventListener {

    private final AuditService auditService;

    public TaskCreatedEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @EventListener
    public void onTaskCreated(TaskCreatedEvent event) {
        AuditEntry entry = new AuditEntry("TASK_CREATED", "Task created: " + event.title(),
                event.taskId(), event.projectId(), event.occurredAt());

        auditService.record(entry);
    }
}
