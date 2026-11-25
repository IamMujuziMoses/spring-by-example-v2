package com.springbyexample.v2.modelproviderintegration.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.springbyexample.v2.modelproviderintegration.dto.ChatResponse;

/**
 * Provides AI-powered question answering using the configured model provider.
 *
 * @author Mujuzi Moses
 */
@Service
public class ChatAssistantService {

    static final String SYSTEM_PROMPT = """
            You are a software technology assistant.
            Explain software engineering concepts clearly and accurately.
            Use practical examples where appropriate.
            Adapt the level of detail to the user's question.
            If you are uncertain about something, acknowledge the uncertainty.
            """;

    private final ChatClient chatClient;

    public ChatAssistantService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public ChatResponse answerQuestion(String question) {
        String answer = chatClient.prompt().system(SYSTEM_PROMPT).user(question).call().content();

        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException("The AI model returned an empty response.");
        }

        return new ChatResponse(question, answer);
    }
}
