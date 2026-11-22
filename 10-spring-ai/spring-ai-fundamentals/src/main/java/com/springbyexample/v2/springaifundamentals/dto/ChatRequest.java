package com.springbyexample.v2.springaifundamentals.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a chat request submitted by a client.
 *
 * @param message the message to send to the AI model
 *
 * @author Mujuzi Moses
 */
public record ChatRequest(@NotBlank(message = "Message must not be blank") String message) {
}
