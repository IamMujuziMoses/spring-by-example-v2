package com.springbyexample.v2.transactionmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.springbyexample.v2.transactionmanagement.domain.Project;
import com.springbyexample.v2.transactionmanagement.domain.Task;
import com.springbyexample.v2.transactionmanagement.repository.ProjectRepository;
import com.springbyexample.v2.transactionmanagement.repository.TaskRepository;

/**
 * Unit tests for project transaction service business behavior.
 *
 * @author Mujuzi Moses
 */
@ExtendWith(MockitoExtension.class)
public class ProjectTransactionServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TransactionAuditService auditService;

    @InjectMocks
    private ProjectTransactionService projectTransactionService;

    @Test
    void createProjectWithTasks_shouldRejectBlankProjectName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                projectTransactionService.createProjectWithTasks(" ", "Invalid project",
                        List.of("First task")));

        assertEquals("Project name must not be blank", exception.getMessage());
        verifyNoInteractions(projectRepository, taskRepository);
        verify(auditService).recordProjectCreationFailure(" ", "Project name must not be blank");
    }

    @Test
    void createProjectWithTasks_shouldCreateProjectAndTasks() {
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project project = projectTransactionService.createProjectWithTasks("Example project",
                "Example description", List.of("First task"));

        assertEquals("Example project", project.getName());
        verify(projectRepository).save(any(Project.class));
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createProjectWithTasks_shouldRejectBlankTaskTitle() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                projectTransactionService.createProjectWithTasks("Example project", "Testing validation",
                        List.of("First task", " ")));

        assertEquals("Task title must not be blank", exception.getMessage());
        verify(projectRepository).save(any(Project.class));
        verify(taskRepository).save(any(Task.class));
        verify(auditService).recordProjectCreationFailure("Example project",
                "Task title must not be blank");
    }

    @Test
    void createProjectWithTasks_shouldAuditAndRethrowPersistenceFailure() {
        when(taskRepository.save(any(Task.class))).thenThrow(new IllegalStateException("Database write failed"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                projectTransactionService.createProjectWithTasks("Example project",
                        "Testing persistence failure", List.of("First task")));

        assertEquals("Database write failed", exception.getMessage());
        verify(projectRepository).save(any(Project.class));
        verify(taskRepository).save(any(Task.class));
        verify(auditService).recordProjectCreationFailure("Example project", "Database write failed");
        verifyNoMoreInteractions(auditService);
    }

    @Test
    void createProjectWithTasks_shouldRejectNullProjectName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                projectTransactionService.createProjectWithTasks(null, "Invalid project",
                        List.of("First task")));

        assertEquals("Project name must not be blank", exception.getMessage());
        verifyNoInteractions(projectRepository, taskRepository);
        verify(auditService).recordProjectCreationFailure(null, "Project name must not be blank");
    }

    @Test
    void createProjectWithTasks_shouldRejectNullTaskTitle() {
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                projectTransactionService.createProjectWithTasks("Example project", "Testing validation",
                        Arrays.asList("First task", null)));

        assertEquals("Task title must not be blank", exception.getMessage());
        verify(projectRepository).save(any(Project.class));
        verify(taskRepository).save(any(Task.class));
        verify(auditService).recordProjectCreationFailure("Example project", "Task title must not be blank");
    }
}
