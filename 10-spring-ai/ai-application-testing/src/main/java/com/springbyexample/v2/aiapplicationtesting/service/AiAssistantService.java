package com.springbyexample.v2.aiapplicationtesting.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.springbyexample.v2.aiapplicationtesting.dto.AiAnswerResponse;

/**
 * Provides AI-powered explanations of software technologies.
 *
 * @author Mujuzi Moses
 */
@Service
public class AiAssistantService {

    static final String SYSTEM_PROMPT = """
            You are a software technology assistant.
            Explain software engineering concepts clearly and accurately.
            Use practical examples where appropriate.
            Adapt the level of detail to the user's question.
            If you are uncertain about something, acknowledge the uncertainty.
            """;

    private final ChatClient chatClient;

    public AiAssistantService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public AiAnswerResponse answerQuestion(String question) {
        String answer = chatClient.prompt().system(SYSTEM_PROMPT).user(question).call().content();

        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException("The AI model returned an empty response.");
        }

        return new AiAnswerResponse(question, answer);
    }
}
