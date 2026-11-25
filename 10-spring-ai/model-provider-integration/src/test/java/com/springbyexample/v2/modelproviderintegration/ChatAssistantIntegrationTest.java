package com.springbyexample.v2.modelproviderintegration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import com.springbyexample.v2.modelproviderintegration.service.ChatAssistantService;

/**
 * Integration tests for the Spring AI ChatClient interaction.
 *
 * @author Mujuzi Moses
 */
@ExtendWith(MockitoExtension.class)
public class ChatAssistantIntegrationTest {

    private static final String QUESTION = "What is Spring Boot?";

    private static final String ANSWER = "Spring Boot simplifies the development of Spring applications.";

    @Mock
    private ChatModel chatModel;

    private ChatAssistantService chatAssistantService;

    @BeforeEach
    void setUp() {
        when(chatModel.getOptions()).thenReturn(ChatOptions.builder().build());
        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel);
        chatAssistantService = new ChatAssistantService(chatClientBuilder);
    }

    @Test
    void shouldSendSystemAndUserMessagesToChatModel() {
        ChatResponse modelResponse = ChatResponse.builder()
                .generations(List.of(new Generation(new AssistantMessage(ANSWER))))
                .metadata(ChatResponseMetadata.builder().model("test-model").build()).build();

        when(chatModel.call(any(Prompt.class))).thenReturn(modelResponse);

        com.springbyexample.v2.modelproviderintegration.dto.ChatResponse response =
                chatAssistantService.answerQuestion(QUESTION);

        assertThat(response.question()).isEqualTo(QUESTION);
        assertThat(response.answer()).isEqualTo(ANSWER);

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);

        verify(chatModel).call(promptCaptor.capture());

        List<Message> messages = promptCaptor.getValue().getInstructions();

        assertThat(messages).anySatisfy(message -> {
            assertThat(message.getMessageType()).isEqualTo(MessageType.SYSTEM);
            assertThat(message.getText()).contains("You are a software technology assistant.");
        });

        assertThat(messages).anySatisfy(message -> {
            assertThat(message.getMessageType()).isEqualTo(MessageType.USER);
            assertThat(message.getText()).isEqualTo(QUESTION);
        });
    }
}
