package com.springbyexample.v2.toolcalling.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.springbyexample.v2.toolcalling.dto.ToolCallingResponse;
import com.springbyexample.v2.toolcalling.service.ToolCallingService;

import tools.jackson.databind.ObjectMapper;

/**
 * Tests the tool calling REST controller.
 *
 * @author Mujuzi Moses
 */
public class ToolCallingControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private ToolCallingService toolCallingService;

    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        toolCallingService = mock(ToolCallingService.class);
        objectMapper = new ObjectMapper();
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        ToolCallingController controller = new ToolCallingController(toolCallingService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ToolCallingExceptionHandler())
                .setValidator(validator).build();
    }

    @AfterEach
    void tearDown() {
        validator.destroy();
    }

    @Test
    void shouldAnswerQuestion() throws Exception {
        String question = "What is 125 multiplied by 8?";
        String answer = "125 multiplied by 8 is 1000.";

        when(toolCallingService.answerQuestion(question)).thenReturn(new ToolCallingResponse(question, answer));

        mockMvc.perform(post("/api/tool-calling").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ToolCallingRequestTestData(question))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value(question))
                .andExpect(jsonPath("$.answer").value(answer));
    }

    @Test
    void shouldReturnBadRequestWhenQuestionIsMissing() throws Exception {
        mockMvc.perform(post("/api/tool-calling").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenQuestionIsBlank() throws Exception {
        mockMvc.perform(post("/api/tool-calling").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadGatewayWhenToolCallingFails() throws Exception {
        when(toolCallingService.answerQuestion(anyString())).thenThrow(new IllegalStateException("Chat model unavailable"));

        mockMvc.perform(post("/api/tool-calling").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "What is 125 multiplied by 8?"
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("Tool Calling Operation Failed"))
                .andExpect(jsonPath("$.detail")
                        .value("The AI service could not complete the requested operation."));
    }

    /**
     * Minimal request data used by the successful controller test.
     *
     * @param question the user's question
     */
    private record ToolCallingRequestTestData(String question) {
    }
}
