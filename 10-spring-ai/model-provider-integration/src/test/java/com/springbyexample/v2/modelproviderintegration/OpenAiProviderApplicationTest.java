package com.springbyexample.v2.modelproviderintegration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.springbyexample.v2.modelproviderintegration.service.ChatAssistantService;

/**
 * Tests application configuration with the OpenAI provider.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
@ActiveProfiles("openai")
@TestPropertySource(properties = "spring.ai.openai.api-key=test-api-key")
public class OpenAiProviderApplicationTest {

    @Autowired
    private ChatAssistantService chatAssistantService;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ChatModel chatModel;

    @Test
    void shouldLoadApplicationContextWithOpenAiProvider() {
        assertThat(chatAssistantService).isNotNull();
        assertThat(chatClientBuilder).isNotNull();
    }

    @Test
    void shouldUseOpenAiChatModelWhenOpenAiProfileIsActive() {
        assertThat(chatModel).isInstanceOf(OpenAiChatModel.class);
    }
}
