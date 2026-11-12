package com.springbyexample.v2.integrationtesting.service;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.integrationtesting.model.OrderSummary;

/**
 * Processes order summaries.
 *
 * @author Mujuzi Moses
 */
@Service
public class OrderProcessingService {

    /**
     * Processes an order summary.
     *
     * @param orderSummary order summary to process
     * @return processed order summary
     */
    public OrderSummary process(OrderSummary orderSummary) {
        if (orderSummary.quantity() <= 0) {
            throw new IllegalArgumentException("Order quantity must be greater than zero");
        }

        return new OrderSummary(orderSummary.orderId(), orderSummary.quantity(), "Processed: "
                + orderSummary.description());
    }
}
