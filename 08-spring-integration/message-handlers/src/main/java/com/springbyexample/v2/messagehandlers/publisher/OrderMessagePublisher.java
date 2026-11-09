package com.springbyexample.v2.messagehandlers.publisher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.messagehandlers.model.OrderMessage;

/**
 * Publishes order messages to the message handler channel.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderMessagePublisher {

    private final MessageChannel orderHandlerChannel;

    public OrderMessagePublisher(@Qualifier("orderHandlerChannel") MessageChannel orderHandlerChannel) {
        this.orderHandlerChannel = orderHandlerChannel;
    }

    public boolean publish(OrderMessage order, String source) {
        Message<OrderMessage> message = MessageBuilder.withPayload(order).setHeader("source", source).build();

        return orderHandlerChannel.send(message);
    }

}
