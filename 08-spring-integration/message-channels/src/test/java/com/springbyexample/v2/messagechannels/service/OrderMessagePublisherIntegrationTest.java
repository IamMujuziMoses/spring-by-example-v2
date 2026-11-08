package com.springbyexample.v2.messagechannels.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.messagechannels.MessageChannelsApplication;
import com.springbyexample.v2.messagechannels.domain.OrderMessage;
import com.springbyexample.v2.messagechannels.support.MessageChannelSupport;

/**
 * Integration tests for publishing order messages through configured channels.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(classes = MessageChannelsApplication.class)
public class OrderMessagePublisherIntegrationTest {

    @Autowired
    private OrderMessagePublisher publisher;

    @Autowired
    private MessageChannelSupport support;

    @BeforeEach
    void setUp() {
        support.reset();
    }

    @Test
    void publishOrder_shouldDeliverMessageToDirectHandler() {
        OrderMessage order = new OrderMessage(1L, "First order");

        publisher.publishOrder(order);

        assertEquals(List.of(order), support.directMessages());
        assertTrue(support.auditMessages().isEmpty());
        assertTrue(support.notificationMessages().isEmpty());
    }

    @Test
    void publishOrderEvent_shouldDeliverMessageToAllSubscribers() {
        OrderMessage order = new OrderMessage(1L, "First order");

        publisher.publishOrderEvent(order);

        assertTrue(support.directMessages().isEmpty());
        assertEquals(List.of(order), support.auditMessages());
        assertEquals(List.of(order), support.notificationMessages());
    }
}
