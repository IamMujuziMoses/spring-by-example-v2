package com.springbyexample.v2.chatmodels.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaChatOptions;

/**
 * Unit tests for {@link ChatModelService}.
 *
 * @author Mujuzi Moses
 */
public class ChatModelServiceTest {

    private static final String MODEL_NAME = "llama3.2";

    private static final String MESSAGE = "Explain Spring AI";

    private static final String GENERATED_RESPONSE = "Spring AI simplifies AI integration.";

    private ChatModel chatModel;

    private ChatResponse chatResponse;

    private Generation generation;

    private AssistantMessage assistantMessage;

    private ChatModelService chatModelService;

    @BeforeEach
    void setUp() {
        chatModel = mock(ChatModel.class);
        chatResponse = mock(ChatResponse.class);
        generation = mock(Generation.class);
        assistantMessage = mock(AssistantMessage.class);

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        when(assistantMessage.getText()).thenReturn(GENERATED_RESPONSE);

        chatModelService = new ChatModelService(chatModel, MODEL_NAME);
    }

    @Test
    void shouldReturnGeneratedResponseUsingConfiguredModelOptions() {
        String response = chatModelService.chat(MESSAGE);

        assertEquals(GENERATED_RESPONSE, response);
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void shouldReturnGeneratedResponseUsingCustomTemperature() {
        String response = chatModelService.chat(MESSAGE, 0.2);

        assertEquals(GENERATED_RESPONSE, response);
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void shouldUseRequestSpecificTemperature() {
        chatModelService.chat(MESSAGE, 0.2);

        var promptCaptor = org.mockito.ArgumentCaptor.forClass(Prompt.class);

        verify(chatModel).call(promptCaptor.capture());

        OllamaChatOptions options = assertInstanceOf(OllamaChatOptions.class, promptCaptor.getValue().getOptions());

        assertEquals(0.2, options.getTemperature());
    }

    @Test
    void shouldPropagateExceptionWhenChatModelFails() {
        when(chatModel.call(any(Prompt.class))).thenThrow(new IllegalStateException("AI model unavailable"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> chatModelService.chat(MESSAGE));

        assertEquals("AI model unavailable", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChatModelReturnsNullResponse() {
        when(chatModel.call(any(Prompt.class))).thenReturn(null);

        var exception = assertThrows(IllegalStateException.class, () -> chatModelService.chat(MESSAGE));

        assertEquals("The chat model returned an incomplete response.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenChatModelReturnsNoGeneration() {
        when(chatResponse.getResult()).thenReturn(null);

        var exception = assertThrows(IllegalStateException.class, () -> chatModelService.chat(MESSAGE));

        assertEquals("The chat model returned an incomplete response.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenGenerationHasNoOutput() {
        when(generation.getOutput()).thenReturn(null);

        var exception = assertThrows(IllegalStateException.class, () -> chatModelService.chat(MESSAGE));

        assertEquals("The chat model returned an incomplete response.", exception.getMessage());
    }

    @Test
    void shouldReturnEmptyResponseWhenAssistantMessageIsEmpty() {
        when(assistantMessage.getText()).thenReturn("");

        var response = chatModelService.chat(MESSAGE);

        assertEquals("", response);
    }

    @Test
    void shouldPassUserMessageToChatModel() {
        var promptCaptor = ArgumentCaptor.forClass(Prompt.class);

        chatModelService.chat(MESSAGE);

        verify(chatModel).call(promptCaptor.capture());

        var prompt = promptCaptor.getValue();

        assertEquals(1, prompt.getInstructions().size());

        var userMessage = assertInstanceOf(UserMessage.class, prompt.getInstructions().getFirst());

        assertEquals(MESSAGE, userMessage.getText());
    }

    @Test
    void shouldUseConfiguredModelWhenCustomTemperatureIsProvided() {
        var promptCaptor = ArgumentCaptor.forClass(Prompt.class);

        chatModelService.chat(MESSAGE, 0.2);

        verify(chatModel).call(promptCaptor.capture());

        var options = assertInstanceOf(OllamaChatOptions.class, promptCaptor.getValue().getOptions());

        assertEquals(MODEL_NAME, options.getModel());
    }

}
