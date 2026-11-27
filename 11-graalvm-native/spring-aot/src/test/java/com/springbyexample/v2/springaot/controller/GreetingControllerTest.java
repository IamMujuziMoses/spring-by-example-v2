package com.springbyexample.v2.springaot.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.springbyexample.v2.springaot.service.GreetingService;

/**
 * Tests the greeting REST controller.
 *
 * @author Mujuzi Moses
 */
@WebMvcTest(GreetingController.class)
public class GreetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GreetingService greetingService;

    @Test
    void shouldReturnGreeting() throws Exception {
        given(greetingService.greet("Mujuzi")).willReturn("Hello, Mujuzi! Welcome to Spring AOT.");

        mockMvc.perform(get("/api/aot/greeting").param("name", "Mujuzi"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Mujuzi! Welcome to Spring AOT."));
    }

    @Test
    void shouldUseDefaultNameWhenNameIsNotProvided() throws Exception {
        given(greetingService.greet("Spring AOT")).willReturn("Hello, Spring AOT! Welcome to Spring AOT.");

        mockMvc.perform(get("/api/aot/greeting"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Spring AOT! Welcome to Spring AOT."));
    }
}
