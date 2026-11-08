package com.springbyexample.v2.messagechannels.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.springbyexample.v2.messagechannels.domain.OrderMessage;

/**
 * Tests the message broadcasting behavior of a PublishSubscribeChannel.
 *
 * @author Mujuzi Moses
 */
public class PublishSubscribeChannelTest {

    @Test
    void send_shouldBroadcastMessageToAllSubscribers() {
        PublishSubscribeChannel channel = new PublishSubscribeChannel();

        List<Message<?>> firstSubscriberMessages = new ArrayList<>();
        List<Message<?>> secondSubscriberMessages = new ArrayList<>();

        channel.subscribe(firstSubscriberMessages::add);
        channel.subscribe(secondSubscriberMessages::add);

        OrderMessage order = new OrderMessage(1L, "First order");

        boolean sent = channel.send(MessageBuilder.withPayload(order).build());

        assertTrue(sent);
        assertEquals(1, firstSubscriberMessages.size());
        assertEquals(1, secondSubscriberMessages.size());

        assertEquals(order, firstSubscriberMessages.getFirst().getPayload());
        assertEquals(order, secondSubscriberMessages.getFirst().getPayload());
    }

    @Test
    void send_shouldBroadcastMultipleMessagesToAllSubscribers() {
        PublishSubscribeChannel channel = new PublishSubscribeChannel();

        List<Message<?>> firstSubscriberMessages = new ArrayList<>();
        List<Message<?>> secondSubscriberMessages = new ArrayList<>();

        channel.subscribe(firstSubscriberMessages::add);
        channel.subscribe(secondSubscriberMessages::add);

        OrderMessage firstOrder = new OrderMessage(1L, "First order");
        OrderMessage secondOrder = new OrderMessage(2L, "Second order");

        channel.send(MessageBuilder.withPayload(firstOrder).build());
        channel.send(MessageBuilder.withPayload(secondOrder).build());

        assertEquals(2, firstSubscriberMessages.size());
        assertEquals(2, secondSubscriberMessages.size());

        assertEquals(List.of(firstOrder, secondOrder), firstSubscriberMessages.stream()
                        .map(message -> (OrderMessage) message.getPayload()).toList());

        assertEquals(List.of(firstOrder, secondOrder),
                secondSubscriberMessages.stream().map(message -> (OrderMessage) message.getPayload())
                        .toList());
    }
}
