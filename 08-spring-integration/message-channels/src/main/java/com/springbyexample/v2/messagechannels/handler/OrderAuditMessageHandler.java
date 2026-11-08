package com.springbyexample.v2.messagechannels.handler;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.messagechannels.domain.OrderMessage;
import com.springbyexample.v2.messagechannels.support.MessageChannelSupport;

/**
 * Handles order messages for auditing.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderAuditMessageHandler implements MessageHandler {

    private final MessageChannelSupport support;

    /**
     * Creates the order audit message handler.
     *
     * @param support message recording support
     */
    public OrderAuditMessageHandler(MessageChannelSupport support) {
        this.support = support;
    }

    /**
     * Processes an order message for auditing.
     *
     * @param message incoming message
     */
    @Override
    public void handleMessage(Message<?> message) {
        OrderMessage order = (OrderMessage) message.getPayload();
        support.recordAuditMessage(order);
    }

}
