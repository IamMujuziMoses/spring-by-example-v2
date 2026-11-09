package com.springbyexample.v2.messagehandlers.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.springbyexample.v2.messagehandlers.model.OrderMessage;

/**
 * Stores processed orders for observing message handler behavior.
 *
 * @author Mujuzi Moses
 */
@Component
public class MessageHandlerSupport {

    private final List<ProcessedOrder> processedOrders = new CopyOnWriteArrayList<>();

    public void recordProcessedOrder(OrderMessage order, String source) {
        processedOrders.add(new ProcessedOrder(order, source));
    }

    public List<ProcessedOrder> processedOrders() {
        return List.copyOf(processedOrders);
    }

    public void clear() {
        processedOrders.clear();
    }

    /**
     * Represents an order processed by the message handler.
     *
     * @param order  the processed order
     * @param source the message source
     */
    public record ProcessedOrder(OrderMessage order, String source) {
    }

}
