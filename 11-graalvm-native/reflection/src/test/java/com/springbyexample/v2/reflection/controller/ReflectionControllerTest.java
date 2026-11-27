package com.springbyexample.v2.reflection.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.springbyexample.v2.reflection.service.ReflectionService;

/**
 * Tests the reflection REST controller.
 *
 * @author Mujuzi Moses
 */
@WebMvcTest(ReflectionController.class)
public class ReflectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReflectionService reflectionService;

    @Test
    void shouldReturnGreetingMessage() throws Exception {
        given(reflectionService.getMessage("Greeting")).willReturn("Hello! Welcome to Spring Reflection.");

        mockMvc.perform(get("/api/native/reflection/message").param("type", "Greeting"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello! Welcome to Spring Reflection."));
    }

    @Test
    void shouldReturnWelcomeMessage() throws Exception {
        given(reflectionService.getMessage("Welcome"))
                .willReturn("Welcome! This example demonstrates reflection in Spring.");

        mockMvc.perform(get("/api/native/reflection/message").param("type", "Welcome"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome! This example demonstrates reflection in Spring."));
    }

    @Test
    void shouldUseGreetingAsDefaultMessageType() throws Exception {
        given(reflectionService.getMessage("Greeting")).willReturn("Hello! Welcome to Spring Reflection.");

        mockMvc.perform(get("/api/native/reflection/message")).andExpect(status().isOk())
                .andExpect(content().string("Hello! Welcome to Spring Reflection."));
    }
}
