package com.springbyexample.v2.prompttemplates.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

import com.springbyexample.v2.prompttemplates.service.PromptTemplateService;

/**
 * Web layer tests for {@link PromptTemplateController}.
 *
 * @author Mujuzi Moses
 */
public class PromptTemplateControllerTest {

    private static final String TECHNOLOGY = "Spring Boot";

    private static final String AUDIENCE = "beginner developers";

    private static final String DETAIL_LEVEL = "simple";

    private static final String QUESTION = "Explain dependency injection";

    private static final String GENERATED_RESPONSE = "Dependency injection is a design technique...";

    private MockMvc mockMvc;

    private PromptTemplateService promptTemplateService;

    @BeforeEach
    void setUp() {
        promptTemplateService = mock(PromptTemplateService.class);

        var controller = new PromptTemplateController(promptTemplateService);
        var exceptionHandler = new PromptTemplateExceptionHandler();
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(exceptionHandler).setValidator(validator)
                .build();
    }

    @Test
    void shouldReturnGeneratedResponse() throws Exception {
        when(promptTemplateService.generateExplanation(TECHNOLOGY, AUDIENCE, DETAIL_LEVEL, QUESTION))
                .thenReturn(GENERATED_RESPONSE);

        mockMvc.perform(post("/api/prompt-templates").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "technology": "Spring Boot",
                                    "audience": "beginner developers",
                                    "detailLevel": "simple",
                                    "question": "Explain dependency injection"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(GENERATED_RESPONSE));

        verify(promptTemplateService).generateExplanation(TECHNOLOGY, AUDIENCE, DETAIL_LEVEL, QUESTION);
    }

    @Test
    void shouldRejectBlankTechnology() throws Exception {
        mockMvc.perform(post("/api/prompt-templates").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "technology": "",
                                "audience": "beginner developers",
                                "detailLevel": "simple",
                                "question": "Explain dependency injection"
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(promptTemplateService);
    }

    @Test
    void shouldRejectBlankAudience() throws Exception {
        mockMvc.perform(post("/api/prompt-templates").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "technology": "Spring Boot",
                                    "audience": " ",
                                    "detailLevel": "simple",
                                    "question": "Explain dependency injection"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectBlankDetailLevel() throws Exception {
        mockMvc.perform(post("/api/prompt-templates").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "technology": "Spring Boot",
                                    "audience": "beginner developers",
                                    "detailLevel": "",
                                    "question": "Explain dependency injection"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectBlankQuestion() throws Exception {
        mockMvc.perform(post("/api/prompt-templates").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "technology": "Spring Boot",
                                    "audience": "beginner developers",
                                    "detailLevel": "simple",
                                    "question": " "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRequestWithMissingFields() throws Exception {
        mockMvc.perform(post("/api/prompt-templates").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "technology": "Spring Boot"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/prompt-templates").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "technology":
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnServiceUnavailableWhenChatModelFails() throws Exception {
        when(promptTemplateService.generateExplanation(TECHNOLOGY, AUDIENCE, DETAIL_LEVEL, QUESTION))
                .thenThrow(new IllegalStateException("Model unavailable"));

        mockMvc.perform(post("/api/prompt-templates").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "technology": "Spring Boot",
                                    "audience": "beginner developers",
                                    "detailLevel": "simple",
                                    "question": "Explain dependency injection"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail")
                        .value("The chat model is currently unable to process the request."));
    }

    @Test
    void shouldRejectNullTechnology() throws Exception {
        mockMvc.perform(post("/api/prompt-templates").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "technology": null,
                                "audience": "beginner developers",
                                "detailLevel": "simple",
                                "question": "Explain dependency injection"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectNullAudience() throws Exception {
        mockMvc.perform(post("/api/prompt-templates").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "technology": "Spring Boot",
                                "audience": null,
                                "detailLevel": "simple",
                                "question": "Explain dependency injection"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectNullDetailLevel() throws Exception {
        mockMvc.perform(post("/api/prompt-templates").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "technology": "Spring Boot",
                                "audience": "beginner developers",
                                "detailLevel": null,
                                "question": "Explain dependency injection"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectNullQuestion() throws Exception {
        mockMvc.perform(post("/api/prompt-templates").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "technology": "Spring Boot",
                                "audience": "beginner developers",
                                "detailLevel": "simple",
                                "question": null
                            }
                            """))
                .andExpect(status().isBadRequest());
    }
}
