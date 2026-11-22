package com.springbyexample.v2.springaifundamentals.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.springbyexample.v2.springaifundamentals.service.AiChatService;

/**
 * Tests for {@link ChatController}.
 *
 * @author Mujuzi Moses
 */
public class ChatControllerTest {

    private AiChatService aiChatService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        aiChatService = mock(AiChatService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(aiChatService))
                .setControllerAdvice(new AiChatExceptionHandler()).setValidator(validator).build();
    }

    @Test
    void shouldReturnChatResponse() throws Exception {
        when(aiChatService.chat("Explain Spring AI")).thenReturn("Spring AI simplifies AI integration.");

        mockMvc.perform(post("/api/ai/chat").contentType("application/json")
                        .content("""
                                {
                                    "message": "Explain Spring AI"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Spring AI simplifies AI integration."));
    }

    @Test
    void shouldRejectBlankMessage() throws Exception {
        mockMvc.perform(post("/api/ai/chat").contentType("application/json")
                        .content("""
                                {
                                    "message": " "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRequestWithMissingMessage() throws Exception {
        mockMvc.perform(post("/api/ai/chat").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRequestWithNullMessage() throws Exception {
        mockMvc.perform(post("/api/ai/chat").contentType("application/json")
                        .content("""
                                {
                                    "message": null
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/ai/chat").contentType("application/json")
                        .content("""
                                {
                                    "message":
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnServiceUnavailableWhenAiServiceFails() throws Exception {
        when(aiChatService.chat("Explain Spring AI")).thenThrow(new IllegalStateException("AI model unavailable"));

        mockMvc.perform(post("/api/ai/chat").contentType("application/json")
                        .content("""
                            {
                                "message": "Explain Spring AI"
                            }
                            """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("The AI service is temporarily unavailable."));
    }
}
