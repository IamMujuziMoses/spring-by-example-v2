package com.springbyexample.v2.modulararchitecture.task;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.springbyexample.v2.modulararchitecture.project.ProjectNotFoundException;
import com.springbyexample.v2.modulararchitecture.task.internal.TaskController;

/**
 * Tests the task REST controller.
 *
 * @author Mujuzi Moses
 */
@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void createTask_shouldReturnCreatedTask() throws Exception {
        given(taskService.createTask(1L, "Write tests"))
                .willReturn(new Task(10L, 1L, "Write tests", false));

        mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": 1,
                                  "title": "Write tests"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(taskService).createTask(1L, "Write tests");
    }

    @Test
    void getTasks_shouldReturnTasks() throws Exception {
        given(taskService.getTasks()).willReturn(
                List.of(
                        new Task(1L, 10L, "First task", false),
                        new Task(2L, 10L, "Second task", true)
                )
        );

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("First task"))
                .andExpect(jsonPath("$[1].completed").value(true));
    }

    @Test
    void getTask_shouldReturnTask() throws Exception {
        Task task = new Task(1L, 10L, "Write tests", false);
        given(taskService.getTask(1L)).willReturn(task);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.projectId").value(10))
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(taskService).getTask(1L);
    }

    @Test
    void getTask_shouldReturnNotFoundForUnknownTask() throws Exception {
        given(taskService.getTask(99L)).willThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/tasks/99")).andExpect(status().isNotFound());
    }

    @Test
    void getTasksForProject_shouldReturnProjectTasks() throws Exception {
        given(taskService.getTasksForProject(1L)).willReturn(
                List.of(new Task(10L, 1L, "Write tests", false)));

        mockMvc.perform(get("/api/projects/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].projectId").value(1))
                .andExpect(jsonPath("$[0].title").value("Write tests"));
    }

    @Test
    void createTask_shouldReturnNotFoundForUnknownProject() throws Exception {
        given(taskService.createTask(99L, "Write tests")).willThrow(new ProjectNotFoundException(99L));

        mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": 99,
                                  "title": "Write tests"
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
