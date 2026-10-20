package com.springbyexample.v2.springtestslices;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Mujuzi Moses
 */
@WebMvcTest(GreetingController.class)
public class GreetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GreetingService greetingService;

    @Test
    void shouldReturnDefaultGreeting() throws Exception {
        when(greetingService.greet("Spring")).thenReturn("Hello, Spring!");

        mockMvc.perform(get("/greetings"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Spring!"));

        verify(greetingService).greet("Spring");
    }

    @Test
    void shouldReturnGreetingForProvidedName() throws Exception {
        when(greetingService.greet("Moses")).thenReturn("Hello, Moses!");

        mockMvc.perform(get("/greetings").param("name", "Moses"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Moses!"));

        verify(greetingService).greet("Moses");
    }

    @Test
    void shouldReturnGreetingForAnotherName() throws Exception {
        when(greetingService.greet("Java")).thenReturn("Hello, Java!");

        mockMvc.perform(get("/greetings").param("name", "Java"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Java!"));

        verify(greetingService).greet("Java");
    }
}
