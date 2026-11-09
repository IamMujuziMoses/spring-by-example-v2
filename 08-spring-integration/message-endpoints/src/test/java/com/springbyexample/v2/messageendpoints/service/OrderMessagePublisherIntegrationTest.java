package com.springbyexample.v2.messageendpoints.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.messageendpoints.MessageEndpointsApplication;
import com.springbyexample.v2.messageendpoints.domain.OrderMessage;
import com.springbyexample.v2.messageendpoints.support.MessageEndpointSupport;

/**
 * Integration tests for publishing messages through the order endpoint channel.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(classes = MessageEndpointsApplication.class)
public class OrderMessagePublisherIntegrationTest {

    @Autowired
    private OrderMessagePublisher publisher;

    @Autowired
    private MessageEndpointSupport support;

    @BeforeEach
    void setUp() {
        support.reset();
    }

    @Test
    void publishOrder_shouldReturnTrueWhenMessageIsDelivered() {
        OrderMessage order = new OrderMessage(1L, "First order");

        boolean sent = publisher.publishOrder(order);

        assertTrue(sent);
        assertEquals(1, support.processedOrders().size());
    }

    @Test
    void publishOrder_shouldPreserveMessagePayload() {
        OrderMessage order = new OrderMessage(1L, "First order");

        publisher.publishOrder(order);

        assertEquals(order, support.processedOrders().getFirst());
    }
}
