package com.springbyexample.v2.applicationtesting;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the complete Application Testing application.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationTestingApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Verifies that the Spring application context starts successfully.
    }

    @Test
    void greet_shouldReturnDefaultGreeting() throws Exception {
        mockMvc.perform(get("/api/greetings")).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Spring!"));
    }

    @Test
    void greet_shouldReturnPersonalizedGreeting() throws Exception {
        mockMvc.perform(get("/api/greetings").param("name", "Moses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Moses!"));
    }

    @Test
    void greet_shouldReturnJsonResponse() throws Exception {
        mockMvc.perform(get("/api/greetings")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                    {
                        "message": "Hello, Spring!"
                    }
                    """));
    }
}
