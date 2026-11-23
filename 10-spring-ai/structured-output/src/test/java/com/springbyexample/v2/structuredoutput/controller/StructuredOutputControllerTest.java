package com.springbyexample.v2.structuredoutput.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.springbyexample.v2.structuredoutput.model.TechnologyRecommendation;
import com.springbyexample.v2.structuredoutput.service.StructuredOutputService;

/**
 * Web layer tests for {@link StructuredOutputController}.
 *
 * @author Mujuzi Moses
 */
public class StructuredOutputControllerTest {

    private static final String ENDPOINT = "/api/structured-output";

    private static final String VALID_REQUEST = """
            {
              "technology": "Spring Boot",
              "useCase": "Building REST APIs",
              "audience": "Java developers"
            }
            """;

    private StructuredOutputService structuredOutputService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        structuredOutputService = mock(StructuredOutputService.class);

        var controller = new StructuredOutputController(structuredOutputService);
        var exceptionHandler = new StructuredOutputExceptionHandler();
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(exceptionHandler).setValidator(validator)
                .build();
    }

    @Test
    void shouldReturnStructuredRecommendation() throws Exception {
        var recommendation = new TechnologyRecommendation("Spring Boot", "Java application framework",
                "A framework for building standalone Spring applications.",
                List.of("Auto-configuration", "Embedded servers"),
                List.of("Reduces configuration"),
                List.of("Requires Spring knowledge"),
                List.of("REST APIs", "Microservices")
        );

        when(structuredOutputService.generateRecommendation("Spring Boot", "Building REST APIs",
                "Java developers")).thenReturn(recommendation);

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "name": "Spring Boot",
                          "category": "Java application framework",
                          "description": "A framework for building standalone Spring applications.",
                          "keyFeatures": [
                            "Auto-configuration",
                            "Embedded servers"
                          ],
                          "advantages": [
                            "Reduces configuration"
                          ],
                          "limitations": [
                            "Requires Spring knowledge"
                          ],
                          "suitableUseCases": [
                            "REST APIs",
                            "Microservices"
                          ]
                        }
                        """));

        verify(structuredOutputService).generateRecommendation("Spring Boot", "Building REST APIs",
                "Java developers");
    }

    @Test
    void shouldRejectBlankTechnology() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technology": " ",
                                  "useCase": "Building REST APIs",
                                  "audience": "Java developers"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(structuredOutputService);
    }

    @Test
    void shouldRejectBlankUseCase() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technology": "Spring Boot",
                                  "useCase": " ",
                                  "audience": "Java developers"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(structuredOutputService);
    }

    @Test
    void shouldRejectBlankAudience() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technology": "Spring Boot",
                                  "useCase": "Building REST APIs",
                                  "audience": " "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(structuredOutputService);
    }

    @Test
    void shouldRejectMissingTechnology() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "useCase": "Building REST APIs",
                                  "audience": "Java developers"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(structuredOutputService);
    }

    @Test
    void shouldRejectMissingUseCase() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technology": "Spring Boot",
                                  "audience": "Java developers"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(structuredOutputService);
    }

    @Test
    void shouldRejectMissingAudience() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technology": "Spring Boot",
                                  "useCase": "Building REST APIs"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(structuredOutputService);
    }

    @Test
    void shouldRejectNullTechnology() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technology": null,
                                  "useCase": "Building REST APIs",
                                  "audience": "Java developers"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(structuredOutputService);
    }

    @Test
    void shouldRejectNullUseCase() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technology": "Spring Boot",
                                  "useCase": null,
                                  "audience": "Java developers"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(structuredOutputService);
    }

    @Test
    void shouldRejectNullAudience() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technology": "Spring Boot",
                                  "useCase": "Building REST APIs",
                                  "audience": null
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(structuredOutputService);
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "technology": "Spring Boot",
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(structuredOutputService);
    }

    @Test
    void shouldReturnBadGatewayWhenStructuredOutputFails() throws Exception {

        when(structuredOutputService.generateRecommendation("Spring Boot", "Building REST APIs",
                "Java developers")).thenThrow(new IllegalStateException("Invalid model response"));

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        verify(structuredOutputService).generateRecommendation("Spring Boot", "Building REST APIs",
                "Java developers");
    }
}
