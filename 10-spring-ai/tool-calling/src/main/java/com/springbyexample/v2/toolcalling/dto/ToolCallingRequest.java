package com.springbyexample.v2.toolcalling.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a request to execute an AI tool-assisted operation.
 *
 * @param question the user's question
 *
 * @author Mujuzi Moses
 */
public record ToolCallingRequest(@NotBlank(message = "Question must not be blank") String question) {
}
