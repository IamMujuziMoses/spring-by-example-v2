package com.springbyexample.v2.messagechannels.service;

import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.springbyexample.v2.messagechannels.domain.OrderMessage;

/**
 * Publishes order messages to configured message channels.
 *
 * @author Mujuzi Moses
 */
@Service
public class OrderMessagePublisher {

    private final DirectChannel orderChannel;

    private final PublishSubscribeChannel orderEventsChannel;

    /**
     * Creates the order message publisher.
     *
     * @param orderChannel       point-to-point channel
     * @param orderEventsChannel publish-subscribe channel
     */
    public OrderMessagePublisher(DirectChannel orderChannel, PublishSubscribeChannel orderEventsChannel) {
        this.orderChannel = orderChannel;
        this.orderEventsChannel = orderEventsChannel;
    }

    /**
     * Publishes an order message to the point-to-point channel.
     *
     * @param message order message
     * @return whether the message was accepted
     */
    public boolean publishOrder(OrderMessage message) {
        return orderChannel.send(MessageBuilder.withPayload(message).build());
    }

    /**
     * Publishes an order event to all subscribers.
     *
     * @param message order event
     * @return whether the message was accepted
     */
    public boolean publishOrderEvent(OrderMessage message) {
        return orderEventsChannel.send(
                MessageBuilder.withPayload(message).build()
        );
    }
}
