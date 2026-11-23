package com.springbyexample.v2.structuredoutput.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request containing the information used to generate a technology recommendation.
 *
 * @param technology the technology to evaluate
 * @param useCase    the intended use case
 * @param audience   the intended audience
 *
 * @author Mujuzi Moses
 */
public record TechnologyRequest(@NotBlank String technology, @NotBlank String useCase, @NotBlank String audience) {
}
