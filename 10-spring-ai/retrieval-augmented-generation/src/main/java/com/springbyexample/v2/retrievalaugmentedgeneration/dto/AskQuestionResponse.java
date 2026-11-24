package com.springbyexample.v2.retrievalaugmentedgeneration.dto;

/**
 * Response containing the generated answer.
 *
 * @param question the original question
 * @param answer   the generated answer
 *
 * @author Mujuzi Moses
 */
public record AskQuestionResponse(String question, String answer) {
}
