package com.springbyexample.v2.routers.publisher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.routers.model.OrderMessage;

/**
 * Publishes order messages to the router input channel.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderMessagePublisher {

    private final MessageChannel orderInputChannel;

    public OrderMessagePublisher(@Qualifier("orderInputChannel") MessageChannel orderInputChannel) {
        this.orderInputChannel = orderInputChannel;
    }

    public void publish(OrderMessage order) {
        orderInputChannel.send(MessageBuilder.withPayload(order).build());
    }
}
