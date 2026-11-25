package com.springbyexample.v2.toolcalling.dto;

/**
 * Represents the response from an AI tool-assisted operation.
 *
 * @param question the original user question
 * @param answer   the generated answer
 *
 * @author Mujuzi Moses
 */
public record ToolCallingResponse(String question, String answer) {
}
