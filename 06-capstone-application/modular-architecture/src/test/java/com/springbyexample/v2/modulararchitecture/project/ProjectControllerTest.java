package com.springbyexample.v2.modulararchitecture.project;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.springbyexample.v2.modulararchitecture.project.internal.ProjectController;

/**
 * Tests the project REST controller.
 *
 * @author Mujuzi Moses
 */
@WebMvcTest(ProjectController.class)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void createProject_shouldReturnCreatedProject() throws Exception {
        Project project = new Project(1L, "Spring Modulith", "Learn modular architecture");

        given(projectService.createProject("Spring Modulith", "Learn modular architecture"))
                .willReturn(project);

        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Spring Modulith",
                          "description": "Learn modular architecture"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Spring Modulith"))
                .andExpect(jsonPath("$.description")
                        .value("Learn modular architecture"));

        verify(projectService).createProject("Spring Modulith", "Learn modular architecture");
    }

    @Test
    void getProjects_shouldReturnProjects() throws Exception {
        given(projectService.getProjects()).willReturn(
                java.util.List.of(
                        new Project(1L, "First", "First project"),
                        new Project(2L, "Second", "Second project")
                )
        );

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("First"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Second"));

        verify(projectService).getProjects();
    }

    @Test
    void getProject_shouldReturnNotFoundForUnknownProject() throws Exception {
        given(projectService.getProject(99L)).willThrow(new ProjectNotFoundException(99L));

        mockMvc.perform(get("/api/projects/99")).andExpect(status().isNotFound());
    }
}
