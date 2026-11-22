package com.springbyexample.v2.springaifundamentals.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Provides AI-powered chat functionality using Spring AI.
 *
 * @author Mujuzi Moses
 */
@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Sends a message to the configured AI model and returns its response.
     *
     * @param message the message to send
     * @return the generated response
     */
    public String chat(String message) {
        return chatClient.prompt().user(message).call().content();
    }
}
