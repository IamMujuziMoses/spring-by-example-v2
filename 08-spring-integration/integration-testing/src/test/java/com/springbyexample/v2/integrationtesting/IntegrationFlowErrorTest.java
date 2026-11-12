package com.springbyexample.v2.integrationtesting;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.MessageBuilder;

import com.springbyexample.v2.integrationtesting.model.OrderMessage;

/**
 * Integration tests for error handling in the order processing flow.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class IntegrationFlowErrorTest {

    @Autowired
    private MessageChannel ordersInputChannel;

    @Autowired
    private QueueChannel ordersOutputChannel;

    /**
     * Verifies that an exception from the service activator propagates
     * and no successful result reaches the output channel.
     */
    @Test
    void orderIntegrationFlow_shouldPropagateProcessingException() {
        OrderMessage order = new OrderMessage("ORDER-001", 0);
        Message<OrderMessage> message = MessageBuilder.withPayload(order).build();
        MessageHandlingException exception = assertThrows(MessageHandlingException.class,
                () -> ordersInputChannel.send(message));

        assertNotNull(exception.getCause());
        assertNull(ordersOutputChannel.receive(0));
    }
}
