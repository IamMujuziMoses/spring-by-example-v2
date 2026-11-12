package com.springbyexample.v2.integrationtesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import com.springbyexample.v2.integrationtesting.model.OrderMessage;
import com.springbyexample.v2.integrationtesting.model.OrderSummary;

/**
 * Integration tests for asynchronous order processing.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class AsynchronousFlowTest {

    private static final long RECEIVE_TIMEOUT = 3_000L;

    @Autowired
    private MessageChannel asyncOrdersInputChannel;

    @Autowired
    private QueueChannel asyncOrdersOutputChannel;

    /**
     * Verifies that an order submitted through the asynchronous flow
     * is processed and delivered to the output channel.
     *
     * @throws InterruptedException if the test thread is interrupted
     */
    @Test
    void asyncOrderIntegrationFlow_shouldProcessOrderAsynchronously() throws InterruptedException {
        OrderMessage order = new OrderMessage("ORDER-ASYNC-001", 5);
        Message<OrderMessage> message = MessageBuilder.withPayload(order).build();
        boolean sent = asyncOrdersInputChannel.send(message);

        assertTrue(sent);

        Message<?> receivedMessage = asyncOrdersOutputChannel.receive(RECEIVE_TIMEOUT);

        assertNotNull(receivedMessage);
        assertInstanceOf(OrderSummary.class, receivedMessage.getPayload());

        OrderSummary summary = (OrderSummary) receivedMessage.getPayload();

        assertEquals("ORDER-ASYNC-001", summary.orderId());
        assertEquals(5, summary.quantity());
        assertEquals("Processed: Order containing 5 items", summary.description());
    }
}
