package com.springbyexample.v2.batchtesting.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.springbyexample.v2.batchtesting.domain.Order;
import com.springbyexample.v2.batchtesting.domain.ProcessedOrder;

/**
 * Provides sample data and records output for the batch testing example.
 *
 * @author Mujuzi Moses
 */
@Component
public class BatchTestingSupport {

    private static final List<Order> ORDERS = List.of(
            new Order(1L, "first order"),
            new Order(2L, "second order"),
            new Order(3L, "third order"),
            new Order(4L, "fourth order"),
            new Order(5L, "fifth order")
    );

    private final List<ProcessedOrder> writtenOrders = new CopyOnWriteArrayList<>();

    private volatile int summaryProcessedCount = -1;

    public List<Order> orders() {
        return ORDERS;
    }

    public void recordWrittenOrders(List<? extends ProcessedOrder> orders) {
        writtenOrders.addAll(orders);
    }

    public List<ProcessedOrder> writtenOrders() {
        return List.copyOf(writtenOrders);
    }

    public void recordSummary(int processedCount) {
        summaryProcessedCount = processedCount;
    }

    public int summaryProcessedCount() {
        return summaryProcessedCount;
    }

    public void reset() {
        writtenOrders.clear();
        summaryProcessedCount = -1;
    }
}
