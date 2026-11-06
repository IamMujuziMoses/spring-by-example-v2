package com.springbyexample.v2.skipandretry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * Holds sample batch scenarios and exposes processing observations.
 *
 * @author Mujuzi Moses
 */
@Component
public class SkipAndRetrySupport {

    private final Map<Long, AtomicInteger> processingAttempts = new ConcurrentHashMap<>();

    private final List<ProcessedOrder> writtenOrders = new CopyOnWriteArrayList<>();

    public List<CustomerOrder> ordersFor(String scenario) {
        return switch (scenario) {
            case "retry-exhausted" -> List.of(
                    new CustomerOrder(1L, "Order 1", false, 3));
            case "skip-exhausted" -> List.of(
                    new CustomerOrder(1L, "Order 1", true, 0),
                    new CustomerOrder(2L, "Order 2", true, 0),
                    new CustomerOrder(3L, "Order 3", true, 0));
            case "success" -> List.of(
                    new CustomerOrder(1L, "Order 1", false, 0),
                    new CustomerOrder(2L, "Invalid order", true, 0),
                    new CustomerOrder(3L, "Order 3", false, 2),
                    new CustomerOrder(4L, "Order 4", false, 0));
            default -> throw new IllegalArgumentException("Unknown batch scenario: " + scenario);
        };
    }

    public int recordProcessingAttempt(long orderId) {
        return processingAttempts.computeIfAbsent(orderId, ignored -> new AtomicInteger()).incrementAndGet();
    }

    public int processingAttemptsFor(long orderId) {
        AtomicInteger attempts = processingAttempts.get(orderId);
        return attempts == null ? 0 : attempts.get();
    }

    public void recordWrittenOrders(List<? extends ProcessedOrder> orders) {
        writtenOrders.addAll(orders);
    }

    public List<ProcessedOrder> writtenOrders() {
        return List.copyOf(writtenOrders);
    }

    public void reset() {
        processingAttempts.clear();
        writtenOrders.clear();
    }
}
