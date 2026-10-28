package com.springbyexample.v2.restapis.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.Project;
import com.springbyexample.v2.applicationdesignandarchitecture.domain.project.ProjectId;
import com.springbyexample.v2.springdataandpersistence.service.ProjectPersistenceService;

/**
 * Web-layer tests for project REST endpoints.
 *
 * @author Mujuzi Moses
 */
@WebMvcTest(ProjectController.class)
@Import(ApiExceptionHandler.class)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectPersistenceService projectService;

    @Test
    void shouldCreateProjectAndReturnLocationHeader() throws Exception {
        Project project = new Project(new ProjectId("project-1"), "Capstone Application",
                "A Spring application");

        when(projectService.createProject(new ProjectId("project-1"), "Capstone Application",
                "A Spring application")).thenReturn(project);

        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "project-1",
                                  "name": "Capstone Application",
                                  "description": "A Spring application"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/projects/project-1")))
                .andExpect(jsonPath("$.id").value("project-1"))
                .andExpect(jsonPath("$.name").value("Capstone Application"))
                .andExpect(jsonPath("$.description").value("A Spring application"));

        verify(projectService).createProject(new ProjectId("project-1"), "Capstone Application",
                "A Spring application");
    }

    @Test
    void shouldRetrieveProject() throws Exception {
        Project project = new Project(new ProjectId("project-1"), "Capstone Application",
                "A Spring application");

        when(projectService.getProject(new ProjectId("project-1"))).thenReturn(project);

        mockMvc.perform(get("/api/projects/project-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("project-1"))
                .andExpect(jsonPath("$.name").value("Capstone Application"));

        verify(projectService).getProject(new ProjectId("project-1"));
    }

    @Test
    void shouldRejectProjectWithBlankId() throws Exception {
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": " ",
                                  "name": "Capstone Application",
                                  "description": "A Spring application"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"));

        org.mockito.Mockito.verifyNoInteractions(projectService);
    }

    @Test
    void shouldRejectProjectWithBlankName() throws Exception {
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "project-1",
                                  "name": " ",
                                  "description": "A Spring application"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"));

        org.mockito.Mockito.verifyNoInteractions(projectService);
    }

    @Test
    void shouldRejectProjectWithNameLongerThan255Characters() throws Exception {
        String longName = "a".repeat(256);
        String request = """
                {
                  "id": "project-1",
                  "name": "%s",
                  "description": "A Spring application"
                }
                """.formatted(longName);

        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"));

        org.mockito.Mockito.verifyNoInteractions(projectService);
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Request body is malformed or contains invalid values"));

        org.mockito.Mockito.verifyNoInteractions(projectService);
    }
}
