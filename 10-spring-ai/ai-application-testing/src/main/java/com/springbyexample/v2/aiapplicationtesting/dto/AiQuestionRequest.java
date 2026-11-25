package com.springbyexample.v2.aiapplicationtesting.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a question submitted to the AI assistant.
 *
 * @author Mujuzi Moses
 */
public record AiQuestionRequest(@NotBlank(message = "Question must not be blank") String question) {
}
