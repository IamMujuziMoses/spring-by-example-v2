package com.springbyexample.v2.serviceactivators.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

import com.springbyexample.v2.serviceactivators.model.OrderProcessingResult;

/**
 * Records order processing results received from the output channel.
 *
 * @author Mujuzi Moses
 */
public class OrderProcessingSupport implements MessageHandler {

    private final List<ProcessedOrder> processedOrders = new CopyOnWriteArrayList<>();

    @Override
    public void handleMessage(Message<?> message) {
        if (!(message.getPayload() instanceof OrderProcessingResult result)) {
            throw new IllegalArgumentException("Unsupported message payload");
        }

        String source = message.getHeaders().get("source", String.class);

        processedOrders.add(new ProcessedOrder(result, source));
    }

    public List<ProcessedOrder> getProcessedOrders() {
        return List.copyOf(processedOrders);
    }

    public void clear() {
        processedOrders.clear();
    }

    /**
     * Represents a processed order and its originating source.
     *
     * @param result the order processing result
     * @param source the message source
     */
    public record ProcessedOrder(OrderProcessingResult result, String source) {
    }
}
