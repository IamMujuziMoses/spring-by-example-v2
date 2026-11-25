package com.springbyexample.v2.toolcalling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import com.springbyexample.v2.toolcalling.dto.ToolCallingResponse;
import com.springbyexample.v2.toolcalling.tools.CalculatorTools;

/**
 * Tests AI tool calling service behavior.
 *
 * @author Mujuzi Moses
 */
public class ToolCallingServiceTest {

    private ChatClient.Builder chatClientBuilder;

    private ChatClient chatClient;

    private ChatClient.ChatClientRequestSpec requestSpec;

    private ChatClient.CallResponseSpec responseSpec;

    private CalculatorTools calculatorTools;

    private ToolCallingService toolCallingService;

    @BeforeEach
    void setUp() {
        chatClientBuilder = mock(ChatClient.Builder.class);
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        responseSpec = mock(ChatClient.CallResponseSpec.class);
        calculatorTools = mock(CalculatorTools.class);

        when(chatClientBuilder.build()).thenReturn(chatClient);

        toolCallingService = new ToolCallingService(chatClientBuilder, calculatorTools);
    }

    @Test
    void shouldBuildChatClient() {
        verify(chatClientBuilder).build();
    }

    @Test
    void shouldAnswerQuestionUsingCalculatorTools() {
        String question = "What is 125 multiplied by 8?";
        String answer = "125 multiplied by 8 is 1000.";

        mockChatClientResponse(answer);

        ToolCallingResponse response = toolCallingService.answerQuestion(question);

        assertThat(response.question()).isEqualTo(question);
        assertThat(response.answer()).isEqualTo(answer);
        verify(requestSpec).tools(calculatorTools);
    }

    @Test
    void shouldThrowExceptionWhenChatModelReturnsNullResponse() {
        mockChatClientResponse(null);

        assertThatThrownBy(() -> toolCallingService.answerQuestion("What is 125 multiplied by 8?"))
                .isInstanceOf(IllegalStateException.class).hasMessage("The AI model returned an empty response.");
    }

    @Test
    void shouldThrowExceptionWhenChatModelReturnsBlankResponse() {
        mockChatClientResponse("   ");

        assertThatThrownBy(() -> toolCallingService.answerQuestion("What is 125 multiplied by 8?"))
                .isInstanceOf(IllegalStateException.class).hasMessage("The AI model returned an empty response.");
    }

    @Test
    void shouldPropagateExceptionWhenChatModelFails() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.tools(calculatorTools)).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new IllegalStateException("Chat model unavailable"));

        assertThatThrownBy(() -> toolCallingService.answerQuestion("What is 125 multiplied by 8?"))
                .isInstanceOf(IllegalStateException.class).hasMessage("Chat model unavailable");
    }

    private void mockChatClientResponse(String answer) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.tools(calculatorTools)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(answer);
    }
}
