package com.springbyexample.v2.prompttemplates.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Provides prompt template functionality using Spring AI.
 *
 * @author Mujuzi Moses
 */
@Service
public class PromptTemplateService {

    private final ChatModel chatModel;

    private final PromptTemplate promptTemplate;

    public PromptTemplateService(ChatModel chatModel,
            @Value("classpath:/prompts/technology-explanation.st") Resource promptResource) throws IOException {

        this.chatModel = chatModel;

        String templateContent = promptResource.getContentAsString(StandardCharsets.UTF_8);

        this.promptTemplate = new PromptTemplate(templateContent);
    }

    /**
     * Generates a response using the technology explanation template.
     *
     * @param technology  the technology to explain
     * @param audience    the intended audience
     * @param detailLevel the desired level of detail
     * @param question    the specific question or instruction
     * @return the generated response
     */
    public String generateExplanation(String technology, String audience, String detailLevel, String question) {

        Map<String, Object> variables = Map.of(
                "technology", technology,
                "audience", audience,
                "detailLevel", detailLevel,
                "question", question
        );

        Prompt prompt = promptTemplate.create(variables);
        ChatResponse response = chatModel.call(prompt);

        return extractResponse(response);
    }

    private String extractResponse(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            throw new IllegalStateException("The chat model returned an incomplete response.");
        }

        return response.getResult().getOutput().getText();
    }
}
