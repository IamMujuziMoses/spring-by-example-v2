package com.springbyexample.v2.transformers.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.transformers.model.OrderSummary;

/**
 * Stores transformed order messages for observing transformer behavior.
 *
 * @author Mujuzi Moses
 */
@Component
public class TransformerSupport implements MessageHandler {

    private final List<ProcessedOrder> processedOrders = new CopyOnWriteArrayList<>();

    @Override
    public void handleMessage(Message<?> message) {
        if (!(message.getPayload() instanceof OrderSummary summary)) {
            throw new IllegalArgumentException("Unsupported message payload");
        }

        String source = message.getHeaders().get("source", String.class);

        processedOrders.add(new ProcessedOrder(summary, source));
    }

    public List<ProcessedOrder> processedOrders() {
        return List.copyOf(processedOrders);
    }

    public void clear() {
        processedOrders.clear();
    }

    /**
     * Represents a transformed order and its message source.
     *
     * @param summary the transformed order summary
     * @param source  the original message source
     */
    public record ProcessedOrder(OrderSummary summary, String source) {
    }

}
