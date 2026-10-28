package com.springbyexample.v2.restapis.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.Task;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskId;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.task.TaskStatus;
import com.springbyexample.v2.springdataandpersistence.service.ProjectPersistenceService;
import com.springbyexample.v2.springdataandpersistence.service.TaskPersistenceService;

/**
 * Web-layer tests for task REST endpoints.
 *
 * @author Mujuzi Moses
 */
@WebMvcTest(TaskController.class)
@Import(ApiExceptionHandler.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskPersistenceService taskService;

    @MockitoBean
    private ProjectPersistenceService projectService;

    @Test
    void shouldCreateTaskAndReturnLocationHeader() throws Exception {
        Task task = new Task(new TaskId("task-1"), new ProjectId("project-1"), "Implement REST APIs",
                "Expose project and task operations", TaskStatus.TODO);

        when(taskService.createTask(new TaskId("task-1"), new ProjectId("project-1"),
                "Implement REST APIs", "Expose project and task operations"
        )).thenReturn(task);

        mockMvc.perform(post("/api/projects/project-1/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "task-1",
                                  "title": "Implement REST APIs",
                                  "description": "Expose project and task operations"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/api/tasks/task-1")))
                .andExpect(jsonPath("$.id").value("task-1"))
                .andExpect(jsonPath("$.projectId").value("project-1"))
                .andExpect(jsonPath("$.status").value("TODO"));

        verify(taskService).createTask(new TaskId("task-1"), new ProjectId("project-1"),
                "Implement REST APIs", "Expose project and task operations");
    }

    @Test
    void shouldRetrieveTask() throws Exception {
        Task task = new Task(new TaskId("task-1"), new ProjectId("project-1"), "Implement REST APIs",
                "Expose project and task operations", TaskStatus.TODO);

        when(taskService.getTask(new TaskId("task-1"))).thenReturn(task);

        mockMvc.perform(get("/api/tasks/task-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("task-1"))
                .andExpect(jsonPath("$.projectId").value("project-1"))
                .andExpect(jsonPath("$.title").value("Implement REST APIs"))
                .andExpect(jsonPath("$.status").value("TODO"));

        verify(taskService).getTask(new TaskId("task-1"));
    }

    @Test
    void shouldListTasksForProject() throws Exception {
        Project project = new Project(new ProjectId("project-1"), "Capstone Application",
                "A Spring application");

        Task firstTask = new Task(new TaskId("task-1"), new ProjectId("project-1"), "First task",
                "First description", TaskStatus.TODO);

        Task secondTask = new Task(new TaskId("task-2"), new ProjectId("project-1"), "Second task",
                "Second description", TaskStatus.IN_PROGRESS);

        when(projectService.getProject(new ProjectId("project-1"))).thenReturn(project);
        when(taskService.getTasksForProject(new ProjectId("project-1"))).thenReturn(List.of(firstTask, secondTask));

        mockMvc.perform(get("/api/projects/project-1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("task-1"))
                .andExpect(jsonPath("$[1].id").value("task-2"));

        verify(projectService).getProject(new ProjectId("project-1"));
        verify(taskService).getTasksForProject(new ProjectId("project-1"));
    }

    @Test
    void shouldReturnEmptyArrayWhenProjectHasNoTasks() throws Exception {
        Project project = new Project(new ProjectId("project-1"), "Capstone Application",
                "A Spring application");

        when(projectService.getProject(new ProjectId("project-1"))).thenReturn(project);
        when(taskService.getTasksForProject(new ProjectId("project-1"))).thenReturn(List.of());

        mockMvc.perform(get("/api/projects/project-1/tasks")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldTransitionTaskStatus() throws Exception {
        Task task = new Task(new TaskId("task-1"), new ProjectId("project-1"), "Implement REST APIs",
                "Expose project and task operations", TaskStatus.IN_PROGRESS);

        when(taskService.transitionTask(new TaskId("task-1"), TaskStatus.IN_PROGRESS)).thenReturn(task);

        mockMvc.perform(patch("/api/tasks/task-1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_PROGRESS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("task-1"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(taskService).transitionTask(new TaskId("task-1"), TaskStatus.IN_PROGRESS);
    }

    @Test
    void shouldRejectTaskWithBlankTitle() throws Exception {
        mockMvc.perform(post("/api/projects/project-1/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "task-1",
                                  "title": " ",
                                  "description": "Invalid task"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"));

        org.mockito.Mockito.verifyNoInteractions(taskService);
    }

    @Test
    void shouldRejectUnknownTaskStatus() throws Exception {
        mockMvc.perform(patch("/api/tasks/task-1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "UNKNOWN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Request body is malformed or contains invalid values"));

        org.mockito.Mockito.verifyNoInteractions(taskService);
    }

    @Test
    void shouldRejectMalformedJsonWhenCreatingTask() throws Exception {
        mockMvc.perform(post("/api/projects/project-1/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Request body is malformed or contains invalid values"));

        org.mockito.Mockito.verifyNoInteractions(taskService);
    }
}
