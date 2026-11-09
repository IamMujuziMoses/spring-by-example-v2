package com.springbyexample.v2.messageendpoints.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.springbyexample.v2.messageendpoints.domain.OrderMessage;

/**
 * Publishes order messages to the message endpoint channel.
 *
 * @author Mujuzi Moses
 */
@Service
public class OrderMessagePublisher {

    private final MessageChannel orderEndpointChannel;

    public OrderMessagePublisher(@Qualifier("orderEndpointChannel") MessageChannel orderEndpointChannel) {
        this.orderEndpointChannel = orderEndpointChannel;
    }

    public boolean publishOrder(OrderMessage order) {
        return orderEndpointChannel.send(MessageBuilder.withPayload(order).build());
    }
}
