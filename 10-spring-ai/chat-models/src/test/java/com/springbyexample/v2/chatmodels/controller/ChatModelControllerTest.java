package com.springbyexample.v2.chatmodels.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.springbyexample.v2.chatmodels.service.ChatModelService;

/**
 * Tests for {@link ChatModelController}.
 *
 * @author Mujuzi Moses
 */
public class ChatModelControllerTest {

    private static final String MESSAGE = "Explain Spring AI";

    private static final String GENERATED_RESPONSE = "Spring AI simplifies AI integration.";

    private ChatModelService chatModelService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        chatModelService = mock(ChatModelService.class);
        ChatModelController chatModelController = new ChatModelController(chatModelService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ChatModelController(chatModelService)).setValidator(validator)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(chatModelController).setControllerAdvice(new ChatModelExceptionHandler())
                .setValidator(validator).build();
    }

    @Test
    void shouldReturnChatResponseUsingConfiguredTemperature() throws Exception {
        when(chatModelService.chat(MESSAGE)).thenReturn(GENERATED_RESPONSE);

        mockMvc.perform(post("/api/chat-models").contentType("application/json")
                        .content("""
                                {
                                    "message": "Explain Spring AI"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(GENERATED_RESPONSE));
    }

    @Test
    void shouldReturnChatResponseUsingCustomTemperature() throws Exception {
        when(chatModelService.chat(MESSAGE, 0.2)).thenReturn(GENERATED_RESPONSE);

        mockMvc.perform(post("/api/chat-models").contentType("application/json")
                        .content("""
                                {
                                    "message": "Explain Spring AI",
                                    "temperature": 0.2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(GENERATED_RESPONSE));
    }

    @Test
    void shouldRejectBlankMessage() throws Exception {
        mockMvc.perform(post("/api/chat-models").contentType("application/json")
                        .content("""
                                {
                                    "message": " "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMissingMessage() throws Exception {
        mockMvc.perform(post("/api/chat-models").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectNullMessage() throws Exception {
        mockMvc.perform(post("/api/chat-models").contentType("application/json")
                        .content("""
                                {
                                    "message": null
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectTemperatureBelowMinimum() throws Exception {
        mockMvc.perform(post("/api/chat-models").contentType("application/json")
                        .content("""
                                {
                                    "message": "Explain Spring AI",
                                    "temperature": -0.1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectTemperatureAboveMaximum() throws Exception {
        mockMvc.perform(post("/api/chat-models").contentType("application/json")
                        .content("""
                                {
                                    "message": "Explain Spring AI",
                                    "temperature": 2.1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/chat-models").contentType("application/json")
                        .content("""
                                {
                                    "message":
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAcceptMinimumTemperature() throws Exception {
        when(chatModelService.chat(MESSAGE, 0.0)).thenReturn(GENERATED_RESPONSE);

        mockMvc.perform(post("/api/chat-models").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "message": "Explain Spring AI",
                                "temperature": 0.0
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(GENERATED_RESPONSE));

        verify(chatModelService).chat(MESSAGE, 0.0);
    }

    @Test
    void shouldAcceptMaximumTemperature() throws Exception {
        when(chatModelService.chat(MESSAGE, 2.0)).thenReturn(GENERATED_RESPONSE);

        mockMvc.perform(post("/api/chat-models").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "message": "Explain Spring AI",
                                "temperature": 2.0
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(GENERATED_RESPONSE));

        verify(chatModelService).chat(MESSAGE, 2.0);
    }

    @Test
    void shouldReturnServiceUnavailableWhenChatModelFails() throws Exception {

        when(chatModelService.chat(MESSAGE)).thenThrow(new IllegalStateException("Model unavailable"));

        mockMvc.perform(post("/api/chat-models").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "message": "Explain Spring AI"
                            }
                            """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail")
                        .value("The chat model is currently unable to process the request."));
    }

}
