package com.springbyexample.v2.embeddings.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * Request containing the texts to convert into embeddings.
 *
 * @author Mujuzi Moses
 */
public record EmbeddingRequest(@NotEmpty List<@NotBlank String> texts) {
}
