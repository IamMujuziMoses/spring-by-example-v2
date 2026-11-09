package com.springbyexample.v2.messageendpoints.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.messageendpoints.MessageEndpointsApplication;
import com.springbyexample.v2.messageendpoints.domain.OrderMessage;
import com.springbyexample.v2.messageendpoints.service.OrderMessagePublisher;
import com.springbyexample.v2.messageendpoints.support.MessageEndpointSupport;

/**
 * Integration tests for the configured order message endpoint.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(classes = MessageEndpointsApplication.class)
public class OrderMessageEndpointIntegrationTest {

    @Autowired
    private OrderMessagePublisher publisher;

    @Autowired
    private MessageEndpointSupport support;

    @BeforeEach
    void setUp() {
        support.reset();
    }

    @Test
    void publishOrder_shouldDeliverMessageToEndpoint() {
        OrderMessage order = new OrderMessage(1L, "First order");

        boolean sent = publisher.publishOrder(order);

        assertTrue(sent);
        assertEquals(List.of(order), support.processedOrders());
    }

    @Test
    void publishMultipleOrders_shouldProcessEveryMessage() {
        OrderMessage firstOrder = new OrderMessage(1L, "First order");
        OrderMessage secondOrder = new OrderMessage(2L, "Second order");

        publisher.publishOrder(firstOrder);
        publisher.publishOrder(secondOrder);

        assertEquals(List.of(firstOrder, secondOrder), support.processedOrders());
    }
}
