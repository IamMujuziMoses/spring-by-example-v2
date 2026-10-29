package com.springbyexample.v2.transactionmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.springbyexample.v2.transactionmanagement.domain.Project;
import com.springbyexample.v2.transactionmanagement.domain.Task;
import com.springbyexample.v2.transactionmanagement.domain.TransactionAudit;
import com.springbyexample.v2.transactionmanagement.repository.ProjectRepository;
import com.springbyexample.v2.transactionmanagement.repository.TaskRepository;
import com.springbyexample.v2.transactionmanagement.repository.TransactionAuditRepository;
import com.springbyexample.v2.transactionmanagement.service.ProjectTransactionService;
import com.springbyexample.v2.transactionmanagement.service.TransactionAuditService;

/**
 * Integration tests for project creation and transaction management.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class ProjectTransactionServiceIntegrationTest {

    @Autowired
    private ProjectTransactionService projectTransactionService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TransactionAuditRepository auditRepository;

    @Autowired
    private TransactionAuditService auditService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        auditRepository.deleteAll();
    }

    @Test
    void createProjectWithTasks_shouldCommitProjectAndTasks() {
        Project project = projectTransactionService.createProjectWithTasks("Transaction Example",
                "Demonstrates atomic project creation", List.of("Design domain", "Write integration tests"));

        assertNotNull(project.getId());
        assertEquals(1, projectRepository.count());
        assertEquals(2, taskRepository.count());
        assertEquals(0, auditRepository.count());

        Project persistedProject = projectRepository.findAll().getFirst();

        assertNotNull(persistedProject.getId());
        assertEquals("Transaction Example", persistedProject.getName());
        assertEquals("Demonstrates atomic project creation", persistedProject.getDescription());

        List<Task> persistedTasks = taskRepository.findAll();

        assertEquals(2, persistedTasks.size());
        assertTrue(persistedTasks.stream().anyMatch(task -> "Design domain".equals(task.getTitle())));
        assertTrue(persistedTasks.stream().anyMatch(task -> "Write integration tests".equals(task.getTitle())));
        assertTrue(persistedTasks.stream().allMatch(task ->
                task.getId() != null && task.getProject().getId().equals(persistedProject.getId())));
    }

    @Test
    void createProjectWithTasks_shouldCreateProjectWithoutTasks() {
        Project project = projectTransactionService.createProjectWithTasks("Empty Project",
                "A project with no tasks", List.of());

        assertNotNull(project.getId());
        assertEquals(1, projectRepository.count());
        assertEquals(0, taskRepository.count());
        assertEquals(0, auditRepository.count());
    }

    @Test
    void createProjectWithTasks_shouldRollbackAllChangesAndRecordFailure() {
        assertThrows(IllegalArgumentException.class, () ->
                projectTransactionService.createProjectWithTasks("Failed Project",
                        "This operation should roll back", List.of("First task", " ")));

        assertEquals(0, projectRepository.count());
        assertEquals(0, taskRepository.count());
        assertEquals(1, auditRepository.count());

        TransactionAudit audit = auditRepository.findAll().getFirst();

        assertNotNull(audit.getId());
        assertEquals("PROJECT_CREATION_FAILED", audit.getEventType());
        assertEquals("Failed Project", audit.getProjectName());
        assertEquals("Task title must not be blank", audit.getDetails());
        assertNotNull(audit.getOccurredAt());
    }

    @Test
    void recordProjectCreationFailure_shouldPersistAuditRecord() {
        auditService.recordProjectCreationFailure("Audited Project", "Simulated failure");

        assertEquals(1, auditRepository.count());

        TransactionAudit audit = auditRepository.findAll().getFirst();

        assertNotNull(audit.getId());
        assertEquals("PROJECT_CREATION_FAILED", audit.getEventType());
        assertEquals("Audited Project", audit.getProjectName());
        assertEquals("Simulated failure", audit.getDetails());
        assertNotNull(audit.getOccurredAt());
    }

    @Test
    void recordProjectCreationFailure_shouldCommitIndependentlyOfOuterRollback() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            auditService.recordProjectCreationFailure("Independent Audit",
                    "Outer transaction will roll back");

            status.setRollbackOnly();
        });

        assertEquals(1, auditRepository.count());

        TransactionAudit audit = auditRepository.findAll().getFirst();

        assertEquals("Independent Audit", audit.getProjectName());
        assertEquals("Outer transaction will roll back", audit.getDetails());
    }
}
