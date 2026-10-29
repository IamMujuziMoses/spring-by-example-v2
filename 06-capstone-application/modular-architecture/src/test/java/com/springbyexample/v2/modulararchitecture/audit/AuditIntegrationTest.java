package com.springbyexample.v2.modulararchitecture.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.modulararchitecture.project.Project;
import com.springbyexample.v2.modulararchitecture.project.ProjectNotFoundException;
import com.springbyexample.v2.modulararchitecture.project.ProjectService;
import com.springbyexample.v2.modulararchitecture.task.Task;
import com.springbyexample.v2.modulararchitecture.task.TaskService;

/**
 * Tests event-driven integration with the audit module.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class AuditIntegrationTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AuditService auditService;

    @Test
    void createTask_shouldRecordAuditEntry() {
        Project project = projectService.createProject("Audit example", "Verify event-driven auditing");
        Task task = taskService.createTask(project.id(), "Verify audit event");

        AuditEntry entry = findEntryForTask(task.id());

        assertEquals("TASK_CREATED", entry.eventType());
        assertEquals("Task created: Verify audit event", entry.description());
        assertEquals(task.id(), entry.taskId());
        assertEquals(project.id(), entry.projectId());
        assertNotNull(entry.occurredAt());
    }

    @Test
    void createMultipleTasks_shouldRecordAnEntryForEachTask() {
        Project project = projectService.createProject("Multiple task audit", "Verify multiple audit events");

        Task firstTask = taskService.createTask(project.id(), "First task");
        Task secondTask = taskService.createTask(project.id(), "Second task");

        List<AuditEntry> entries = auditService.getEntries();

        assertTrue(entries.stream().anyMatch(entry -> entry.taskId().equals(firstTask.id())));
        assertTrue(entries.stream().anyMatch(entry -> entry.taskId().equals(secondTask.id())));
    }

    @Test
    void createTaskForUnknownProject_shouldNotRecordAuditEntry() {
        int initialEntryCount = auditService.getEntries().size();

        assertThrows(ProjectNotFoundException.class, () -> taskService.createTask(99L, "Invalid task"));
        assertEquals(initialEntryCount, auditService.getEntries().size());
    }

    private AuditEntry findEntryForTask(Long taskId) {
        return auditService.getEntries().stream().filter(entry -> entry.taskId().equals(taskId))
                .findFirst().orElseThrow(() -> new AssertionError("No audit entry found for task: " + taskId));
    }
}