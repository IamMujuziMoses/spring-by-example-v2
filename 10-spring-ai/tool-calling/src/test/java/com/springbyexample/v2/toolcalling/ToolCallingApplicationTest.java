package com.springbyexample.v2.toolcalling;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.toolcalling.service.ToolCallingService;
import com.springbyexample.v2.toolcalling.tools.CalculatorTools;

/**
 * Tests the Spring AI Tool Calling application context.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class ToolCallingApplicationTest {

    @Autowired
    private ToolCallingService toolCallingService;

    @Autowired
    private CalculatorTools calculatorTools;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Test
    void shouldLoadApplicationContext() {
        assertThat(toolCallingService).isNotNull();
        assertThat(calculatorTools).isNotNull();
        assertThat(chatClientBuilder).isNotNull();
    }
}
