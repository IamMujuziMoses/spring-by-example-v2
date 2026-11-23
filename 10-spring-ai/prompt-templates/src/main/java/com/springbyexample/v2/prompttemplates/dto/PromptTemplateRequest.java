package com.springbyexample.v2.prompttemplates.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a request to generate a response using a prompt template.
 *
 * @param technology  the technology to explain
 * @param audience    the intended audience
 * @param detailLevel the desired level of detail
 * @param question    the specific question or instruction
 *
 * @author Mujuzi Moses
 */
public record PromptTemplateRequest(@NotBlank(message = "Technology must not be blank") String technology,
        @NotBlank(message = "Audience must not be blank") String audience,
        @NotBlank(message = "Detail level must not be blank") String detailLevel,
        @NotBlank(message = "Question must not be blank") String question) {
}
