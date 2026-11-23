package com.springbyexample.v2.prompttemplates.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ByteArrayResource;

/**
 * Unit tests for {@link PromptTemplateService}.
 *
 * @author Mujuzi Moses
 */
public class PromptTemplateServiceTest {

    private static final String TECHNOLOGY = "Spring Boot";

    private static final String AUDIENCE = "beginner developers";

    private static final String DETAIL_LEVEL = "simple";

    private static final String QUESTION = "Explain dependency injection";

    private static final String GENERATED_RESPONSE = "Dependency injection is a design technique...";

    private static final String PROMPT_TEMPLATE = """
            Explain {technology} to {audience}.
            Use a {detailLevel} level of detail.
            Address this request: {question}
            """;

    private ChatModel chatModel;

    private ChatResponse chatResponse;

    private Generation generation;

    private PromptTemplateService promptTemplateService;

    @BeforeEach
    void setUp() throws Exception {
        chatModel = mock(ChatModel.class);
        chatResponse = mock(ChatResponse.class);
        generation = mock(Generation.class);

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(new AssistantMessage(GENERATED_RESPONSE));

        promptTemplateService = new PromptTemplateService(chatModel,
                new ByteArrayResource(PROMPT_TEMPLATE.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void shouldReturnGeneratedResponse() {
        var response = promptTemplateService.generateExplanation(TECHNOLOGY, AUDIENCE, DETAIL_LEVEL, QUESTION);

        assertEquals(GENERATED_RESPONSE, response);
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void shouldRenderAllTemplateVariables() {
        var promptCaptor = ArgumentCaptor.forClass(Prompt.class);

        promptTemplateService.generateExplanation(TECHNOLOGY, AUDIENCE, DETAIL_LEVEL, QUESTION);

        verify(chatModel).call(promptCaptor.capture());

        var prompt = promptCaptor.getValue();
        var userMessage = assertInstanceOf(UserMessage.class, prompt.getUserMessage());

        assertEquals(
                """
                        Explain Spring Boot to beginner developers.
                        Use a simple level of detail.
                        Address this request: Explain dependency injection
                        """,
                userMessage.getText()
        );
    }

    @Test
    void shouldThrowExceptionWhenChatModelReturnsNullResponse() {
        when(chatModel.call(any(Prompt.class))).thenReturn(null);

        var exception = assertThrows(IllegalStateException.class, () ->
                promptTemplateService.generateExplanation(TECHNOLOGY, AUDIENCE, DETAIL_LEVEL, QUESTION));

        assertEquals("The chat model returned an incomplete response.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChatModelReturnsNoGeneration() {
        when(chatResponse.getResult()).thenReturn(null);

        var exception = assertThrows(IllegalStateException.class, () ->
                promptTemplateService.generateExplanation(TECHNOLOGY, AUDIENCE, DETAIL_LEVEL, QUESTION));

        assertEquals("The chat model returned an incomplete response.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenGenerationHasNoOutput() {
        when(generation.getOutput()).thenReturn(null);

        var exception = assertThrows(IllegalStateException.class, () ->
                promptTemplateService.generateExplanation(TECHNOLOGY, AUDIENCE, DETAIL_LEVEL, QUESTION));

        assertEquals("The chat model returned an incomplete response.", exception.getMessage());
    }

    @Test
    void shouldPropagateExceptionWhenChatModelFails() {
        when(chatModel.call(any(Prompt.class))).thenThrow(new IllegalStateException("Model unavailable"));

        var exception = assertThrows(IllegalStateException.class, () ->
                promptTemplateService.generateExplanation(TECHNOLOGY, AUDIENCE, DETAIL_LEVEL, QUESTION));

        assertEquals("Model unavailable", exception.getMessage());
    }

    @Test
    void shouldReturnEmptyResponseWhenAssistantMessageIsEmpty() {
        when(generation.getOutput()).thenReturn(new AssistantMessage(""));

        var response = promptTemplateService.generateExplanation(TECHNOLOGY, AUDIENCE, DETAIL_LEVEL, QUESTION);

        assertEquals("", response);
    }
}
