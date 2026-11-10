package com.springbyexample.v2.filters.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.filters.model.OrderMessage;

/**
 * Records order messages accepted by the filter.
 *
 * @author Mujuzi Moses
 */
@Component
public class FilterSupport implements MessageHandler {

    private final List<ProcessedOrder> processedOrders = new CopyOnWriteArrayList<>();

    @Override
    public void handleMessage(Message<?> message) {
        if (!(message.getPayload() instanceof OrderMessage order)) {
            throw new IllegalArgumentException("Unsupported message payload");
        }

        String source = message.getHeaders().get("source", String.class);

        processedOrders.add(new ProcessedOrder(order, source));
    }

    public List<ProcessedOrder> getProcessedOrders() {
        return List.copyOf(processedOrders);
    }

    public void clear() {
        processedOrders.clear();
    }

    /**
     * Represents an order processed by the downstream handler.
     *
     * @param order  the accepted order
     * @param source the message source
     */
    public record ProcessedOrder(OrderMessage order, String source) {
    }
}
