package com.springbyexample.v2.messaging.msgorderservice.service;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.messaging.msgorderservice.event.OrderCreatedEvent;

/**
 * Publishes order events to the messaging infrastructure.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderEventPublisher {

    private static final String OUTPUT_BINDING = "orderCreated-out-0";

    private final StreamBridge streamBridge;

    public OrderEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * Publishes an order-created event.
     *
     * @param event the event to publish
     * @return whether the message was accepted by the binding
     */
    public boolean publish(OrderCreatedEvent event) {
        return streamBridge.send(OUTPUT_BINDING, event);
    }
}
