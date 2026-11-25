package com.springbyexample.v2.toolcalling.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.springbyexample.v2.toolcalling.dto.ToolCallingResponse;
import com.springbyexample.v2.toolcalling.tools.CalculatorTools;

/**
 * Handles AI interactions that can invoke application-defined tools.
 *
 * @author Mujuzi Moses
 */
@Service
public class ToolCallingService {

    private final ChatClient chatClient;

    private final CalculatorTools calculatorTools;

    public ToolCallingService(ChatClient.Builder chatClientBuilder, CalculatorTools calculatorTools) {
        this.chatClient = chatClientBuilder.build();
        this.calculatorTools = calculatorTools;
    }

    public ToolCallingResponse answerQuestion(String question) {
        String answer = chatClient.prompt().user(question).tools(calculatorTools).call().content();

        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException("The AI model returned an empty response.");
        }

        return new ToolCallingResponse(question, answer);
    }
}
