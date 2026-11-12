package com.springbyexample.v2.integrationtesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import com.springbyexample.v2.integrationtesting.model.OrderMessage;
import com.springbyexample.v2.integrationtesting.model.OrderSummary;

/**
 * Integration tests for the order processing integration flow.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
@SpringIntegrationTest
public class IntegrationFlowTest {

    private static final long RECEIVE_TIMEOUT = 1_000L;

    @Autowired
    private MessageChannel ordersInputChannel;

    @Autowired
    private QueueChannel ordersOutputChannel;

    /**
     * Verifies that an order message is transformed, processed,
     * and delivered to the output channel.
     *
     * @throws InterruptedException if the test thread is interrupted
     */
    @Test
    void orderIntegrationFlow_shouldTransformAndProcessOrder() throws InterruptedException {
        OrderMessage order = new OrderMessage("ORDER-001", 3);
        Message<OrderMessage> message = MessageBuilder.withPayload(order).build();
        boolean sent = ordersInputChannel.send(message);

        assertTrue(sent);

        Message<?> receivedMessage = ordersOutputChannel.receive(RECEIVE_TIMEOUT);

        assertNotNull(receivedMessage);
        assertInstanceOf(OrderSummary.class, receivedMessage.getPayload());

        OrderSummary summary = (OrderSummary) receivedMessage.getPayload();

        assertEquals("ORDER-001", summary.orderId());
        assertEquals(3, summary.quantity());
        assertEquals("Processed: Order containing 3 items", summary.description());
    }
}
