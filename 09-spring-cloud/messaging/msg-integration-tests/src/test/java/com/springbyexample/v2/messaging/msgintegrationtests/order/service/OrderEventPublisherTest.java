package com.springbyexample.v2.messaging.msgintegrationtests.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.stream.function.StreamBridge;

import com.springbyexample.v2.messaging.msgorderservice.event.OrderCreatedEvent;
import com.springbyexample.v2.messaging.msgorderservice.service.OrderEventPublisher;

/**
 * Unit tests for {@link OrderEventPublisher}.
 *
 * @author Mujuzi Moses
 */
public class OrderEventPublisherTest {

    private static final String OUTPUT_BINDING = "orderCreated-out-0";

    private StreamBridge streamBridge;

    private OrderEventPublisher orderEventPublisher;

    @BeforeEach
    void setUp() {
        streamBridge = mock(StreamBridge.class);
        orderEventPublisher = new OrderEventPublisher(streamBridge);
    }

    @Test
    void shouldPublishOrderCreatedEvent() {
        given(streamBridge.send(eq(OUTPUT_BINDING), any(OrderCreatedEvent.class))).willReturn(true);

        OrderCreatedEvent event = createOrderEvent();

        boolean published = orderEventPublisher.publish(event);

        assertThat(published).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPublishingFails() {
        given(streamBridge.send(eq(OUTPUT_BINDING), any(OrderCreatedEvent.class))).willReturn(false);

        boolean published = orderEventPublisher.publish(createOrderEvent());

        assertThat(published).isFalse();
    }

    private OrderCreatedEvent createOrderEvent() {
        return new OrderCreatedEvent("event-001", "order-001", "SKU-1001", 2,
                Instant.now());
    }
}
