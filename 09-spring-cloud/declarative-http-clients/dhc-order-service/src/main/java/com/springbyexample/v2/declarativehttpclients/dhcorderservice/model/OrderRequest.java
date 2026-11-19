package com.springbyexample.v2.declarativehttpclients.dhcorderservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Represents an order creation request.
 *
 * @param orderId   the order identifier
 * @param productId the product identifier
 * @param quantity  the requested quantity
 *
 * @author Mujuzi Moses
 */
public record OrderRequest(@NotBlank String orderId, @NotBlank String productId, @Positive int quantity) {
}
