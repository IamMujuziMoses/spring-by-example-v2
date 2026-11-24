package com.springbyexample.v2.retrievalaugmentedgeneration.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request containing a question to answer using retrieved knowledge.
 *
 * @param question the question to answer
 *
 * @author Mujuzi Moses
 */
public record AskQuestionRequest(@NotBlank String question) {
}
