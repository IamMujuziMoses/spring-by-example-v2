package com.springbyexample.v2.springmvctesting;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Mujuzi Moses
 */
@SpringBootTest
@AutoConfigureMockMvc
public class GreetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnDefaultGreeting() throws Exception {
        mockMvc.perform(get("/greetings"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Spring!"));
    }

    @Test
    void shouldReturnGreetingForProvidedName() throws Exception {
        mockMvc.perform(get("/greetings").param("name", "Moses"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Moses!"));
    }

    @Test
    void shouldReturnGreetingForAnotherName() throws Exception {
        mockMvc.perform(get("/greetings").param("name", "Java"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Java!"));
    }
}
