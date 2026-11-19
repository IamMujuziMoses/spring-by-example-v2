package com.springbyexample.v2.messaging.msgorderservice.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.messaging.msgorderservice.event.OrderCreatedEvent;
import com.springbyexample.v2.messaging.msgorderservice.model.OrderAcceptedResponse;
import com.springbyexample.v2.messaging.msgorderservice.model.OrderRequest;

/**
 * Handles order creation and event publication.
 *
 * @author Mujuzi Moses
 */
@Service
public class OrderService {

    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates an order and publishes an order-created event.
     *
     * @param request the order request
     * @return the order acceptance response
     */
    public OrderAcceptedResponse createOrder(OrderRequest request) {
        OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID().toString(), request.orderId(),
                request.productId(), request.quantity(), Instant.now());

        boolean published = eventPublisher.publish(event);

        if (!published) {
            throw new IllegalStateException("Failed to publish order-created event");
        }

        return new OrderAcceptedResponse(request.orderId(), "ACCEPTED");
    }
}
