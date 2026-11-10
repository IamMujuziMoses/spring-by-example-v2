package com.springbyexample.v2.routers.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.messaging.Message;

import com.springbyexample.v2.routers.model.OrderMessage;

/**
 * Records orders received by their destination channels.
 *
 * @author Mujuzi Moses
 */
public class OrderRoutingSupport {

    private final List<RoutedOrder> routedOrders = new CopyOnWriteArrayList<>();

    public void record(Message<?> message, String destination) {
        if (!(message.getPayload() instanceof OrderMessage order)) {
            throw new IllegalArgumentException("Unsupported message payload");
        }

        routedOrders.add(new RoutedOrder(order, destination));
    }

    public List<RoutedOrder> getRoutedOrders() {
        return List.copyOf(routedOrders);
    }

    public void clear() {
        routedOrders.clear();
    }

    /**
     * Represents an order and the channel to which it was routed.
     *
     * @param order the routed order
     * @param destination the destination channel
     */
    public record RoutedOrder(OrderMessage order, String destination) {
    }
}
