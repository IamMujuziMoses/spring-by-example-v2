package com.springbyexample.v2.integrationbetweenspringtechnologies.ordering;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload for placing an order.
 *
 * @author Mujuzi Moses
 */
public record PlaceOrderRequest(

        @NotNull
        Long productId,

        @Positive
        int quantity

) {
}
