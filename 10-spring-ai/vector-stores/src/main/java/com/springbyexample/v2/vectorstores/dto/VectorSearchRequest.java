package com.springbyexample.v2.vectorstores.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request for searching documents by semantic similarity.
 *
 * @param query natural-language search query
 * @param topK  maximum number of results to return
 *
 * @author Mujuzi Moses
 */
public record VectorSearchRequest(@NotBlank String query, @Min(1) @Max(20) Integer topK) {
}
