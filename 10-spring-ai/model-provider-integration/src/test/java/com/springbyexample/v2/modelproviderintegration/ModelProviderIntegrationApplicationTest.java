package com.springbyexample.v2.modelproviderintegration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.modelproviderintegration.service.ChatAssistantService;

/**
 * Tests the default application configuration.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class ModelProviderIntegrationApplicationTest {

    @Autowired
    private ChatAssistantService chatAssistantService;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ChatModel chatModel;

    @Test
    void shouldLoadApplicationContext() {
        assertThat(chatAssistantService).isNotNull();
        assertThat(chatClientBuilder).isNotNull();
    }

    @Test
    void shouldUseOllamaChatModelByDefault() {
        assertThat(chatModel).isInstanceOf(OllamaChatModel.class);
    }
}
