package com.springbyexample.v2.springmockmvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * @author Mujuzi Moses
 */
public class GreetingControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        GreetingController controller = new GreetingController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

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

    @Test
    void shouldReturnPlainTextContentType() throws Exception {
        mockMvc.perform(get("/greetings"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
    }
}
