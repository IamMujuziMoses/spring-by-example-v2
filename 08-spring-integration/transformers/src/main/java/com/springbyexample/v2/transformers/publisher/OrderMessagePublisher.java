package com.springbyexample.v2.transformers.publisher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.transformers.model.OrderMessage;

/**
 * Publishes order messages to the transformer input channel.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderMessagePublisher {

    private final MessageChannel orderInputChannel;

    public OrderMessagePublisher(@Qualifier("orderInputChannel") MessageChannel orderInputChannel) {
        this.orderInputChannel = orderInputChannel;
    }

    public boolean publish(OrderMessage order, String source) {
        Message<OrderMessage> message = MessageBuilder.withPayload(order).setHeader("source", source).build();

        return orderInputChannel.send(message);
    }
}
