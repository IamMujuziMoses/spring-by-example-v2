package com.springbyexample.v2.aiapplicationtesting.service;

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

import com.springbyexample.v2.aiapplicationtesting.dto.AiAnswerResponse;

/**
 * Tests AI Assistant service behavior.
 *
 * @author Mujuzi Moses
 */
@ExtendWith(MockitoExtension.class)
public class AiAssistantServiceTest {

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

    private AiAssistantService aiAssistantService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        aiAssistantService = new AiAssistantService(chatClientBuilder);
    }

    @Test
    void shouldReturnAnswerWhenModelRespondsSuccessfully() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(QUESTION)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(ANSWER);

        AiAnswerResponse response = aiAssistantService.answerQuestion(QUESTION);

        assertThat(response.question()).isEqualTo(QUESTION);
        assertThat(response.answer()).isEqualTo(ANSWER);

        verify(requestSpec).system(AiAssistantService.SYSTEM_PROMPT);
        verify(requestSpec).user(QUESTION);
    }

    @Test
    void shouldThrowExceptionWhenModelReturnsNullResponse() {
        mockChatClientResponse(null);

        assertThatThrownBy(() -> aiAssistantService.answerQuestion(QUESTION)).isInstanceOf(IllegalStateException.class)
                .hasMessage("The AI model returned an empty response.");
    }

    @Test
    void shouldThrowExceptionWhenModelReturnsBlankResponse() {
        mockChatClientResponse("  ");

        assertThatThrownBy(() -> aiAssistantService.answerQuestion(QUESTION)).isInstanceOf(IllegalStateException.class)
                .hasMessage("The AI model returned an empty response.");
    }

    @Test
    void shouldPropagateExceptionWhenChatClientFails() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(QUESTION)).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new IllegalStateException("Chat model unavailable"));

        assertThatThrownBy(() -> aiAssistantService.answerQuestion(QUESTION)).isInstanceOf(IllegalStateException.class)
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
