package com.springbyexample.v2.chatmodels.service;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Provides chat functionality using Spring AI's ChatModel abstraction.
 *
 * @author Mujuzi Moses
 */
@Service
public class ChatModelService {

    private final ChatModel chatModel;

    private final String modelName;

    public ChatModelService(ChatModel chatModel, @Value("${spring.ai.ollama.chat.model}") String modelName) {
        this.chatModel = chatModel;
        this.modelName = modelName;
    }

    /**
     * Generates a response using the configured model options.
     *
     * @param message the user's message
     * @return the generated response
     */
    public String chat(String message) {
        var prompt = new Prompt(new UserMessage(message));

        return extractResponse(chatModel.call(prompt));
    }

    /**
     * Generates a response using a request-specific temperature.
     *
     * @param message     the user's message
     * @param temperature the temperature for response generation
     * @return the generated response
     */
    public String chat(String message, double temperature) {
        var options = OllamaChatOptions.builder().model(modelName).temperature(temperature).build();

        var prompt = new Prompt(new UserMessage(message), options);

        return extractResponse(chatModel.call(prompt));
    }

    private String extractResponse(ChatResponse response) {

        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            throw new IllegalStateException("The chat model returned an incomplete response.");
        }

        return response.getResult().getOutput().getText();
    }
}
