package com.springbyexample.v2.modelproviderintegration.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a request to the AI assistant.
 *
 * @param question the question submitted by the user
 *
 * @author Mujuzi Moses
 */
public record ChatRequest(@NotBlank(message = "Question must not be blank") String question) {
}
