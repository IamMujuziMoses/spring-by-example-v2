package com.springbyexample.v2.skipandretry;

import java.util.Locale;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.skipandretry.exception.InvalidOrderException;
import com.springbyexample.v2.skipandretry.exception.TransientOrderProcessingException;

/**
 * Validates and transforms customer orders.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderItemProcessor implements ItemProcessor<CustomerOrder, ProcessedOrder> {

    private final SkipAndRetrySupport support;

    public OrderItemProcessor(SkipAndRetrySupport support) {
        this.support = support;
    }

    @Override
    public ProcessedOrder process(CustomerOrder order) {
        if (order.invalid()) {
            throw new InvalidOrderException("Order contains invalid data: " + order.id());
        }

        int attempt = support.recordProcessingAttempt(order.id());

        if (attempt <= order.transientFailuresBeforeSuccess()) {
            throw new TransientOrderProcessingException("Temporary processing failure for order: " + order.id());
        }

        return new ProcessedOrder(order.id(), order.description().toUpperCase(Locale.ROOT));
    }
}
