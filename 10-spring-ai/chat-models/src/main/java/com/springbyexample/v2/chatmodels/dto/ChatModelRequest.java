package com.springbyexample.v2.chatmodels.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a request submitted to a chat model.
 *
 * @param message     the message to send to the chat model
 * @param temperature the optional response generation temperature
 *
 * @author Mujuzi Moses
 */
public record ChatModelRequest(@NotBlank(message = "Message must not be blank") String message,
        @DecimalMin(value = "0.0", message = "Temperature must be at least 0.0")
        @DecimalMax(value = "2.0", message = "Temperature must not exceed 2.0")
        Double temperature) {
}

