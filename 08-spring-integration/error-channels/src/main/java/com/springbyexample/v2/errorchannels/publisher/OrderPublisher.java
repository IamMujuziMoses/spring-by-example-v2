package com.springbyexample.v2.errorchannels.publisher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.errorchannels.model.OrderMessage;

/**
 * Publishes orders to the asynchronous processing channel.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderPublisher {

    private final MessageChannel orderInputChannel;

    private final MessageChannel orderErrorChannel;

    /**
     * Creates an order publisher.
     *
     * @param orderInputChannel asynchronous order input channel
     * @param orderErrorChannel dedicated error channel
     */
    public OrderPublisher(@Qualifier("orderInputChannel") MessageChannel orderInputChannel,
            @Qualifier("orderErrorChannel") MessageChannel orderErrorChannel) {

        this.orderInputChannel = orderInputChannel;
        this.orderErrorChannel = orderErrorChannel;
    }

    /**
     * Publishes an order with its dedicated error channel.
     *
     * @param order order to publish
     * @return whether the channel accepted the message
     */
    public boolean publish(OrderMessage order) {
        Message<OrderMessage> message = MessageBuilder.withPayload(order)
                .setHeader(MessageHeaders.ERROR_CHANNEL, orderErrorChannel).build();

        return orderInputChannel.send(message);
    }
}
