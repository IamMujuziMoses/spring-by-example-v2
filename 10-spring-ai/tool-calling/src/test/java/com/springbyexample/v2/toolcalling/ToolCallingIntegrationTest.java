package com.springbyexample.v2.toolcalling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

import com.springbyexample.v2.toolcalling.service.ToolCallingService;
import com.springbyexample.v2.toolcalling.tools.CalculatorTools;

/**
 * Integration tests for Spring AI tool calling.
 *
 * @author Mujuzi Moses
 */
public class ToolCallingIntegrationTest {

    @Test
    void shouldExecuteCalculatorToolAndReturnFinalAnswer() {
        ChatModel chatModel = mock(ChatModel.class);
        ChatResponse toolCallResponse = createToolCallResponse();
        ChatResponse finalResponse = createFinalResponse();

        when(chatModel.getOptions()).thenReturn(ToolCallingChatOptions.builder().build());
        when(chatModel.call(any(Prompt.class))).thenReturn(toolCallResponse, finalResponse);

        ToolCallingService toolCallingService = new ToolCallingService(ChatClient.builder(chatModel), new CalculatorTools());

        var response = toolCallingService.answerQuestion("What is 6 multiplied by 7?");

        assertThat(response.question()).isEqualTo("What is 6 multiplied by 7?");
        assertThat(response.answer()).isEqualTo("6 multiplied by 7 equals 42.");

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);

        verify(chatModel, times(2)).call(promptCaptor.capture());

        Prompt secondPrompt = promptCaptor.getAllValues().get(1);

        assertThat(secondPrompt.getInstructions()).anySatisfy(message -> assertThat(message.getMessageType())
                .isEqualTo(MessageType.TOOL));
    }

    private ChatResponse createToolCallResponse() {
        AssistantMessage.ToolCall toolCall =
                new AssistantMessage.ToolCall("call-1", "function", "multiply",
                        """
                                {
                                  "firstNumber": 6,
                                  "secondNumber": 7
                                }
                                """
                );

        AssistantMessage assistantMessage = AssistantMessage.builder().toolCalls(List.of(toolCall)).build();

        return new ChatResponse(List.of(new Generation(assistantMessage)));
    }

    private ChatResponse createFinalResponse() {
        AssistantMessage assistantMessage = new AssistantMessage("6 multiplied by 7 equals 42.");

        return new ChatResponse(List.of(new Generation(assistantMessage)));
    }
}
