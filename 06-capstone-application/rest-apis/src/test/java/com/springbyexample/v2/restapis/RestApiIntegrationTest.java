package com.springbyexample.v2.restapis;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the REST API.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RestApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateProjectAndManageItsTasks() throws Exception {
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "rest-project",
                                  "name": "REST API Example",
                                  "description": "Project created through the REST API"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("rest-project"))
                .andExpect(jsonPath("$.name").value("REST API Example"));

        mockMvc.perform(get("/api/projects/rest-project"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("rest-project"));

        mockMvc.perform(post("/api/projects/rest-project/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "rest-task",
                                  "title": "Implement REST endpoints",
                                  "description": "Test task persistence through HTTP"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("rest-task"))
                .andExpect(jsonPath("$.projectId").value("rest-project"))
                .andExpect(jsonPath("$.status").value("TODO"));

        mockMvc.perform(get("/api/projects/rest-project/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("rest-task"));

        mockMvc.perform(patch("/api/tasks/rest-task/status").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_PROGRESS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(get("/api/tasks/rest-task"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldRejectProjectWithBlankName() throws Exception {
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "invalid-project",
                                  "name": " ",
                                  "description": "Invalid project"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details[0]").value("name: must not be blank"));
    }

    @Test
    void shouldReturnNotFoundForUnknownProject() throws Exception {
        mockMvc.perform(get("/api/projects/missing-project")).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForUnknownTask() throws Exception {
        mockMvc.perform(get("/api/tasks/missing-task")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenListingTasksForUnknownProject() throws Exception {
        mockMvc.perform(get("/api/projects/missing-project/tasks")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void shouldRejectInvalidTaskStatusTransition() throws Exception {
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "id": "transition-project",
                  "name": "Transition test",
                  "description": "Project for transition testing"
                }
                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects/transition-project/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "id": "transition-task",
                  "title": "Test transition",
                  "description": "A task for testing invalid transitions"
                }
                """))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/tasks/transition-task/status").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "status": "COMPLETED"
                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
