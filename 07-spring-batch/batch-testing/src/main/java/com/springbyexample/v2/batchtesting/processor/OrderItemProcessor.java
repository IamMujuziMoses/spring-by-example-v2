package com.springbyexample.v2.batchtesting.processor;

import java.util.Locale;
import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.batchtesting.domain.Order;
import com.springbyexample.v2.batchtesting.domain.ProcessedOrder;

/**
 * Transforms orders into processed orders.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderItemProcessor implements ItemProcessor<Order, ProcessedOrder> {

    @Override
    public ProcessedOrder process(@NonNull Order order) {
        Objects.requireNonNull(order, "Order must not be null");

        return new ProcessedOrder(order.id(), order.description().toUpperCase(Locale.ROOT));
    }
}
