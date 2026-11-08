package com.springbyexample.v2.messagechannels.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.springbyexample.v2.messagechannels.domain.OrderMessage;

/**
 * Tests the message delivery behavior of a DirectChannel.
 *
 * @author Mujuzi Moses
 */
public class DirectChannelTest {

    @Test
    void send_shouldDeliverMessageToSingleSubscriber() {
        DirectChannel channel = new DirectChannel();
        List<Message<?>> receivedMessages = new ArrayList<>();

        channel.subscribe(receivedMessages::add);

        OrderMessage order = new OrderMessage(1L, "First order");

        boolean sent = channel.send(MessageBuilder.withPayload(order).build());

        assertTrue(sent);
        assertEquals(1, receivedMessages.size());
        assertEquals(order, receivedMessages.getFirst().getPayload());
    }

    @Test
    void send_shouldDistributeMessagesAcrossSubscribers() {
        DirectChannel channel = new DirectChannel();

        List<Message<?>> firstSubscriberMessages = new ArrayList<>();
        List<Message<?>> secondSubscriberMessages = new ArrayList<>();

        channel.subscribe(firstSubscriberMessages::add);
        channel.subscribe(secondSubscriberMessages::add);

        OrderMessage firstOrder = new OrderMessage(1L, "First order");
        OrderMessage secondOrder = new OrderMessage(2L, "Second order");

        channel.send(MessageBuilder.withPayload(firstOrder).build());
        channel.send(MessageBuilder.withPayload(secondOrder).build());

        assertEquals(1, firstSubscriberMessages.size());
        assertEquals(1, secondSubscriberMessages.size());

        List<OrderMessage> receivedOrders = Stream.concat(firstSubscriberMessages.stream(),
                        secondSubscriberMessages.stream()).map(message -> (OrderMessage) message.getPayload())
                .toList();

        assertEquals(2, receivedOrders.size());
        assertEquals(Set.of(firstOrder, secondOrder), Set.copyOf(receivedOrders));
    }
}
