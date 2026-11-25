package com.springbyexample.v2.modelproviderintegration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import com.springbyexample.v2.modelproviderintegration.dto.ChatResponse;

/**
 * Unit tests for {@link ChatAssistantService}.
 *
 * @author Mujuzi Moses
 */
@ExtendWith(MockitoExtension.class)
public class ChatAssistantServiceTest {

    private static final String QUESTION = "What is Spring Boot?";

    private static final String ANSWER = "Spring Boot simplifies the development of Spring applications.";

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private ChatAssistantService chatAssistantService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        chatAssistantService = new ChatAssistantService(chatClientBuilder);
    }

    @Test
    void shouldReturnAnswerWhenModelRespondsSuccessfully() {
        mockChatClientResponse(ANSWER);

        ChatResponse response = chatAssistantService.answerQuestion(QUESTION);

        assertThat(response.question()).isEqualTo(QUESTION);
        assertThat(response.answer()).isEqualTo(ANSWER);

        verify(requestSpec).system(ChatAssistantService.SYSTEM_PROMPT);
        verify(requestSpec).user(QUESTION);
    }

    @Test
    void shouldThrowExceptionWhenModelReturnsNullResponse() {
        mockChatClientResponse(null);

        assertThatThrownBy(() -> chatAssistantService.answerQuestion(QUESTION)).isInstanceOf(IllegalStateException.class)
                .hasMessage("The AI model returned an empty response.");
    }

    @Test
    void shouldThrowExceptionWhenModelReturnsBlankResponse() {
        mockChatClientResponse("  ");

        assertThatThrownBy(() -> chatAssistantService.answerQuestion(QUESTION)).isInstanceOf(IllegalStateException.class)
                .hasMessage("The AI model returned an empty response.");
    }

    @Test
    void shouldPropagateExceptionWhenChatClientFails() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(QUESTION)).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new IllegalStateException("Chat model unavailable"));

        assertThatThrownBy(() -> chatAssistantService.answerQuestion(QUESTION)).isInstanceOf(IllegalStateException.class)
                .hasMessage("Chat model unavailable");
    }

    private void mockChatClientResponse(String answer) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(QUESTION)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(answer);
    }
}
