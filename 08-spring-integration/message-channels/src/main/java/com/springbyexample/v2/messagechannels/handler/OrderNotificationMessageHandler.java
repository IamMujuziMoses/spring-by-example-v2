package com.springbyexample.v2.messagechannels.handler;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.messagechannels.domain.OrderMessage;
import com.springbyexample.v2.messagechannels.support.MessageChannelSupport;

/**
 * Handles order messages for notifications.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderNotificationMessageHandler implements MessageHandler {

    private final MessageChannelSupport support;

    /**
     * Creates the order notification message handler.
     *
     * @param support message recording support
     */
    public OrderNotificationMessageHandler(MessageChannelSupport support) {
        this.support = support;
    }

    /**
     * Processes an order message for notifications.
     *
     * @param message incoming message
     */
    @Override
    public void handleMessage(Message<?> message) {
        OrderMessage order = (OrderMessage) message.getPayload();
        support.recordNotificationMessage(order);
    }

}
