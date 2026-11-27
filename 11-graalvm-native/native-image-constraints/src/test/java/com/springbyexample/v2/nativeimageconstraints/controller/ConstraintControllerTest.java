package com.springbyexample.v2.nativeimageconstraints.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.springbyexample.v2.nativeimageconstraints.service.ConstraintDemoService;

/**
 * Tests the Native Image constraints REST controller.
 *
 * @author Mujuzi Moses
 */
@WebMvcTest(ConstraintController.class)
public class ConstraintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConstraintDemoService constraintDemoService;

    @Test
    void shouldReturnGreetingMessage() throws Exception {
        given(constraintDemoService.loadMessage("Greeting"))
                .willReturn("Hello! Welcome to GraalVM Native Image Constraints.");

        mockMvc.perform(get("/api/native/constraints/message").param("type", "Greeting"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello! Welcome to GraalVM Native Image Constraints."));
    }

    @Test
    void shouldReturnWelcomeMessage() throws Exception {
        given(constraintDemoService.loadMessage("Welcome"))
                .willReturn("Welcome! This example demonstrates dynamic class loading.");

        mockMvc.perform(get("/api/native/constraints/message").param("type", "Welcome"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome! This example demonstrates dynamic class loading."));
    }

    @Test
    void shouldUseGreetingAsDefaultMessageType() throws Exception {
        given(constraintDemoService.loadMessage("Greeting"))
                .willReturn("Hello! Welcome to GraalVM Native Image Constraints.");

        mockMvc.perform(get("/api/native/constraints/message")).andExpect(status().isOk())
                .andExpect(content().string("Hello! Welcome to GraalVM Native Image Constraints."));
    }
}
