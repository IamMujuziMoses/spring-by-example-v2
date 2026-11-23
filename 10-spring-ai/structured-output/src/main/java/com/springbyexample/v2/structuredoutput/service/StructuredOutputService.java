package com.springbyexample.v2.structuredoutput.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.springbyexample.v2.structuredoutput.model.TechnologyRecommendation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Generates structured technology recommendations using Spring AI.
 *
 * @author Mujuzi Moses
 */
@Service
public class StructuredOutputService {

    private final ChatModel chatModel;

    private final Validator validator;

    private final String promptTemplateContent;

    public StructuredOutputService(ChatModel chatModel, Validator validator,
            @Value("classpath:/prompts/technology-recommendation.st") Resource promptResource) throws IOException {
        this.chatModel = chatModel;
        this.validator = validator;
        this.promptTemplateContent = promptResource.getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * Generates a structured technology recommendation.
     *
     * @param technology the technology to evaluate
     * @param useCase    the intended use case
     * @param audience   the intended audience
     * @return the structured technology recommendation
     */
    public TechnologyRecommendation generateRecommendation(String technology, String useCase, String audience) {
        BeanOutputConverter<TechnologyRecommendation> outputConverter =
                new BeanOutputConverter<>(TechnologyRecommendation.class);

        Map<String, Object> variables = Map.of(
                "technology", technology,
                "useCase", useCase,
                "audience", audience,
                "format", outputConverter.getFormat()
        );

        PromptTemplate promptTemplate = new PromptTemplate(promptTemplateContent);
        Prompt prompt = promptTemplate.create(variables);
        ChatResponse response = chatModel.call(prompt);

        if (response == null || response.getResult() == null || response.getResult().getOutput() == null ||
                response.getResult().getOutput().getText() == null ||
                response.getResult().getOutput().getText().isBlank()) {

            throw new IllegalStateException("The AI model returned an empty response.");
        }

        TechnologyRecommendation recommendation;

        try {
            recommendation = outputConverter.convert(response.getResult().getOutput().getText());
        } catch (RuntimeException exception) {
            throw new IllegalStateException("The AI model returned an invalid structured response.", exception);
        }

        Set<ConstraintViolation<TechnologyRecommendation>> violations = validator.validate(recommendation);

        if (!violations.isEmpty()) {
            throw new IllegalStateException("The AI model returned an incomplete or invalid structured response.");
        }

        return recommendation;
    }
}
