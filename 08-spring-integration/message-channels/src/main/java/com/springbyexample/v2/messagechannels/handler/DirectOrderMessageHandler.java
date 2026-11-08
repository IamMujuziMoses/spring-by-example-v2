package com.springbyexample.v2.messagechannels.handler;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.messagechannels.domain.OrderMessage;
import com.springbyexample.v2.messagechannels.support.MessageChannelSupport;

/**
 * Handles order messages delivered through a point-to-point channel.
 *
 * @author Mujuzi Moses
 */
@Component
public class DirectOrderMessageHandler implements MessageHandler {

    private final MessageChannelSupport support;

    /**
     * Creates the direct order message handler.
     *
     * @param support message recording support
     */
    public DirectOrderMessageHandler(MessageChannelSupport support) {
        this.support = support;
    }

    /**
     * Processes a message received from the direct channel.
     *
     * @param message incoming message
     */
    @Override
    public void handleMessage(Message<?> message) {
        OrderMessage order = (OrderMessage) message.getPayload();
        support.recordDirectMessage(order);
    }

}
