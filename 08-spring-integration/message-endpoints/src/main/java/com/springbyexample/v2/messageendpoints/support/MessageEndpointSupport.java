package com.springbyexample.v2.messageendpoints.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.springbyexample.v2.messageendpoints.domain.OrderMessage;

/**
 * Records orders processed by the message endpoint.
 *
 * @author Mujuzi Moses
 */
@Component
public class MessageEndpointSupport {

    private final List<OrderMessage> processedOrders = new CopyOnWriteArrayList<>();

    public void recordProcessedOrder(OrderMessage order) {
        processedOrders.add(order);
    }

    public List<OrderMessage> processedOrders() {
        return List.copyOf(processedOrders);
    }

    public void reset() {
        processedOrders.clear();
    }
}
