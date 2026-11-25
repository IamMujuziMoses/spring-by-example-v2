package com.springbyexample.v2.aiapplicationtesting;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.aiapplicationtesting.service.AiAssistantService;

/**
 * Tests the Spring AI Application Testing application context.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class AiApplicationTestingApplicationTest {

    @Autowired
    private AiAssistantService aiAssistantService;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Test
    void shouldLoadApplicationContext() {
        assertThat(aiAssistantService).isNotNull();
        assertThat(chatClientBuilder).isNotNull();
    }
}
