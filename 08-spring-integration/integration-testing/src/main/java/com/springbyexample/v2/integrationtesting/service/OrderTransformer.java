package com.springbyexample.v2.integrationtesting.service;

import org.jspecify.annotations.NonNull;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.integrationtesting.model.OrderMessage;
import com.springbyexample.v2.integrationtesting.model.OrderSummary;

/**
 * Transforms incoming order messages into order summaries.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderTransformer implements GenericTransformer<Object, OrderSummary> {

    /**
     * Transforms an order message into an order summary.
     *
     * @param payload incoming message payload
     * @return transformed order summary
     */
    @Override
    public OrderSummary transform(@NonNull Object payload) {
        if (!(payload instanceof OrderMessage(String orderId, int quantity))) {
            throw new IllegalArgumentException("Unsupported message payload");
        }

        return new OrderSummary(orderId, quantity, "Order containing " + quantity + " items");
    }
}
