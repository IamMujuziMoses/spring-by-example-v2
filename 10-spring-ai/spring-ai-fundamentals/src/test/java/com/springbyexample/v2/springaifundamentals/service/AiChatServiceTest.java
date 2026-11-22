package com.springbyexample.v2.springaifundamentals.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Unit tests for {@link AiChatService}.
 *
 * @author Mujuzi Moses
 */
public class AiChatServiceTest {

    private ChatClient.ChatClientRequestSpec requestSpec;

    private ChatClient.CallResponseSpec responseSpec;

    private AiChatService aiChatService;

    @BeforeEach
    void setUp() {
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        responseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user("Explain Spring AI")).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);

        aiChatService = new AiChatService(chatClientBuilder);
    }

    @Test
    void shouldReturnResponseFromChatModel() {
        when(responseSpec.content()).thenReturn("Spring AI simplifies AI integration.");

        String response = aiChatService.chat("Explain Spring AI");

        assertEquals("Spring AI simplifies AI integration.", response);
    }

    @Test
    void shouldReturnEmptyResponseWhenChatModelReturnsNull() {
        when(responseSpec.content()).thenReturn(null);

        String response = aiChatService.chat("Explain Spring AI");

        assertNull(response);
    }

    @Test
    void shouldPropagateExceptionWhenChatModelFails() {
        when(requestSpec.call()).thenThrow(new IllegalStateException("AI model unavailable"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> aiChatService.chat("Explain Spring AI"));

        assertEquals("AI model unavailable", exception.getMessage());
    }
}
