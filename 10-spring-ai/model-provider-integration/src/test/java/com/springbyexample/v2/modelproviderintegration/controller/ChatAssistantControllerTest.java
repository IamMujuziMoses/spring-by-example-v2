package com.springbyexample.v2.modelproviderintegration.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.springbyexample.v2.modelproviderintegration.dto.ChatResponse;
import com.springbyexample.v2.modelproviderintegration.exception.ChatAssistantExceptionHandler;
import com.springbyexample.v2.modelproviderintegration.service.ChatAssistantService;

/**
 * Web layer tests for {@link ChatAssistantController}.
 *
 * @author Mujuzi Moses
 */
@WebMvcTest(ChatAssistantController.class)
@Import(ChatAssistantExceptionHandler.class)
public class ChatAssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatAssistantService chatAssistantService;

    @Test
    void shouldReturnAnswerWhenQuestionIsValid() throws Exception {
        when(chatAssistantService.answerQuestion(anyString()))
                .thenReturn(new ChatResponse("What is Spring Boot?",
                        "Spring Boot simplifies Spring application development."));

        mockMvc.perform(post("/api/model-provider/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "What is Spring Boot?"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("What is Spring Boot?"))
                .andExpect(jsonPath("$.answer")
                        .value("Spring Boot simplifies Spring application development."));
    }

    @Test
    void shouldReturnBadRequestWhenQuestionIsMissing() throws Exception {
        mockMvc.perform(post("/api/model-provider/chat").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenQuestionIsBlank() throws Exception {
        mockMvc.perform(post("/api/model-provider/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyContainsInvalidJson() throws Exception {
        mockMvc.perform(post("/api/model-provider/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question":
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadGatewayWhenAiServiceFails() throws Exception {
        when(chatAssistantService.answerQuestion(anyString()))
                .thenThrow(new IllegalStateException("The AI model returned an empty response."));

        mockMvc.perform(post("/api/model-provider/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "What is Spring Boot?"
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("AI Assistant Operation Failed"))
                .andExpect(jsonPath("$.detail")
                        .value("The AI service could not complete the requested operation."));
    }
}
