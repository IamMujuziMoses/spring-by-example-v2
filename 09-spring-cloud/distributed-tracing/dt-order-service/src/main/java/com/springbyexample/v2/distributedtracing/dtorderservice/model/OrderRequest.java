package com.springbyexample.v2.distributedtracing.dtorderservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Represents an incoming order request.
 *
 * @param orderId   the unique order identifier
 * @param productId the product identifier
 * @param quantity  the requested quantity
 *
 * @author Mujuzi Moses
 */
public record OrderRequest(@NotBlank String orderId, @NotBlank String productId, @Positive int quantity) {
}

