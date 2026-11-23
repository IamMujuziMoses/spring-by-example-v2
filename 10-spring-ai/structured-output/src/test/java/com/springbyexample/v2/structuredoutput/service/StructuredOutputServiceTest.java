package com.springbyexample.v2.structuredoutput.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ByteArrayResource;

import com.springbyexample.v2.structuredoutput.model.TechnologyRecommendation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Unit tests for {@link StructuredOutputService}.
 *
 * @author Mujuzi Moses
 */
public class StructuredOutputServiceTest {

    private static final String TECHNOLOGY = "Spring Boot";

    private static final String USE_CASE = "Building REST APIs";

    private static final String AUDIENCE = "Java developers";

    private static final String PROMPT_TEMPLATE = """
            Recommend {technology} for {useCase}.
            The intended audience is {audience}.
            Return the result using this format:
            {format}
            """;

    private static final String GENERATED_JSON = """
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
                "Requires familiarity with the Spring ecosystem"
              ],
              "suitableUseCases": [
                "REST APIs",
                "Microservices"
              ]
            }
            """;

    private ChatModel chatModel;

    private StructuredOutputService structuredOutputService;

    private ChatResponse chatResponse;

    private Generation generation;

    private ValidatorFactory validatorFactory;

    @BeforeEach
    void setUp() throws Exception {
        chatModel = mock(ChatModel.class);
        chatResponse = mock(ChatResponse.class);
        generation = mock(Generation.class);

        validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        var promptResource = new ByteArrayResource(PROMPT_TEMPLATE.getBytes(StandardCharsets.UTF_8));
        structuredOutputService = new StructuredOutputService(chatModel, validator, promptResource);

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(new AssistantMessage(GENERATED_JSON));
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldReturnStructuredTechnologyRecommendation() {
        TechnologyRecommendation recommendation = structuredOutputService.generateRecommendation(TECHNOLOGY, USE_CASE,
                AUDIENCE);

        assertNotNull(recommendation);
        assertEquals("Spring Boot", recommendation.name());
        assertEquals("Java application framework", recommendation.category());
        assertEquals("A framework for building standalone Spring applications.", recommendation.description());
        assertEquals(List.of("Auto-configuration", "Embedded servers"), recommendation.keyFeatures());
        assertEquals(List.of("Reduces configuration"), recommendation.advantages());
        assertEquals(List.of("Requires familiarity with the Spring ecosystem"), recommendation.limitations());
        assertEquals(List.of("REST APIs", "Microservices"), recommendation.suitableUseCases());
    }

    @Test
    void shouldIncludeRequestVariablesAndFormatInstructionsInPrompt() {
        structuredOutputService.generateRecommendation(TECHNOLOGY, USE_CASE, AUDIENCE);

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);

        verify(chatModel).call(promptCaptor.capture());

        String renderedPrompt = promptCaptor.getValue().getInstructions().getFirst().getText();

        assertNotNull(renderedPrompt);
        assertAll(() -> assertTrue(renderedPrompt.contains(TECHNOLOGY)),
                () -> assertTrue(renderedPrompt.contains(USE_CASE)),
                () -> assertTrue(renderedPrompt.contains(AUDIENCE)),
                () -> assertTrue(renderedPrompt.contains("JSON"))
        );
    }

    @Test
    void shouldThrowExceptionWhenChatModelReturnsNullResponse() {
        when(chatModel.call(any(Prompt.class))).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> structuredOutputService.generateRecommendation(TECHNOLOGY,
                USE_CASE, AUDIENCE));
    }

    @Test
    void shouldThrowExceptionWhenChatModelReturnsNoGeneration() {
        when(chatResponse.getResult()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> structuredOutputService.generateRecommendation(TECHNOLOGY,
                USE_CASE, AUDIENCE));
    }

    @Test
    void shouldThrowExceptionWhenGenerationHasNoOutput() {
        when(generation.getOutput()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> structuredOutputService.generateRecommendation(TECHNOLOGY,
                USE_CASE, AUDIENCE));
    }

    @Test
    void shouldThrowExceptionWhenAssistantOutputIsBlank() {
        when(generation.getOutput()).thenReturn(new AssistantMessage(" "));

        assertThrows(IllegalStateException.class, () -> structuredOutputService.generateRecommendation(TECHNOLOGY,
                USE_CASE, AUDIENCE));
    }

    @Test
    void shouldThrowExceptionWhenAssistantMessageTextIsNull() {
        when(generation.getOutput()).thenReturn(new AssistantMessage((String) null));

        assertThrows(IllegalStateException.class, () -> structuredOutputService.generateRecommendation(TECHNOLOGY,
                USE_CASE, AUDIENCE));
    }

    @Test
    void shouldThrowExceptionWhenModelReturnsInvalidJson() {
        when(generation.getOutput()).thenReturn(new AssistantMessage("This is not valid JSON."));

        assertThrows(IllegalStateException.class, () -> structuredOutputService.generateRecommendation(TECHNOLOGY,
                USE_CASE, AUDIENCE));
    }

    @Test
    void shouldThrowExceptionWhenModelReturnsIncompleteJson() {
        when(generation.getOutput()).thenReturn(
                new AssistantMessage("""
                    {
                      "name": "Spring Boot",
                      "category": "Java application framework"
                    }
                    """)
        );

        assertThrows(IllegalStateException.class, () -> structuredOutputService.generateRecommendation(TECHNOLOGY,
                USE_CASE, AUDIENCE));
    }

    @Test
    void shouldPropagateExceptionWhenChatModelFails() {
        when(chatModel.call(any(Prompt.class))).thenThrow(new IllegalStateException("Model unavailable"));

        assertThrows(IllegalStateException.class, () -> structuredOutputService.generateRecommendation(TECHNOLOGY,
                USE_CASE, AUDIENCE));
    }
}
