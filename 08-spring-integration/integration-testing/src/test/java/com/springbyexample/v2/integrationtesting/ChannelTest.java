package com.springbyexample.v2.integrationtesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import com.springbyexample.v2.integrationtesting.model.OrderMessage;

/**
 * Integration tests for Spring Integration message channels.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class ChannelTest {

    @Autowired
    private MessageChannel ordersInputChannel;

    @Autowired
    private QueueChannel ordersOutputChannel;

    @Autowired
    private MessageChannel asyncOrdersInputChannel;

    /**
     * Verifies that the synchronous input channel is a DirectChannel.
     */
    @Test
    void ordersInputChannel_shouldBeDirectChannel() {
        assertInstanceOf(DirectChannel.class, ordersInputChannel);
    }

    /**
     * Verifies that the output channel is a QueueChannel.
     */
    @Test
    void ordersOutputChannel_shouldBeQueueChannel() {
        assertInstanceOf(QueueChannel.class, ordersOutputChannel);
    }

    /**
     * Verifies that the asynchronous input channel is an ExecutorChannel.
     */
    @Test
    void asyncOrdersInputChannel_shouldBeExecutorChannel() {
        assertInstanceOf(ExecutorChannel.class, asyncOrdersInputChannel);
    }

    /**
     * Verifies that a DirectChannel delivers a message to its subscriber.
     */
    @Test
    void directChannel_shouldDeliverMessageToSubscriber() {
        DirectChannel channel = new DirectChannel();
        AtomicReference<Message<?>> receivedMessage = new AtomicReference<>();

        channel.subscribe(receivedMessage::set);

        OrderMessage order = new OrderMessage("ORDER-001", 2);
        Message<OrderMessage> message = MessageBuilder.withPayload(order).build();

        boolean sent = channel.send(message);

        assertTrue(sent);
        assertNotNull(receivedMessage.get());
        assertEquals(order, receivedMessage.get().getPayload());
    }

    /**
     * Verifies that a QueueChannel retains a message until it is received.
     *
     * @throws InterruptedException if the test thread is interrupted
     */
    @Test
    void queueChannel_shouldStoreMessageUntilReceived() throws InterruptedException {
        QueueChannel channel = new QueueChannel();

        OrderMessage order = new OrderMessage("ORDER-001", 2);
        Message<OrderMessage> message = MessageBuilder.withPayload(order).build();

        boolean sent = channel.send(message);

        assertTrue(sent);

        Message<?> receivedMessage = channel.receive(1_000L);

        assertNotNull(receivedMessage);
        assertEquals(order, receivedMessage.getPayload());
    }
}
