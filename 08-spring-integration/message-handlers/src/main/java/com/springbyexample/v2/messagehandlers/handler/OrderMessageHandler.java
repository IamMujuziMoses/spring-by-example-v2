package com.springbyexample.v2.messagehandlers.handler;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.messagehandlers.model.OrderMessage;
import com.springbyexample.v2.messagehandlers.service.OrderMessageService;

/**
 * Handles incoming order messages.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderMessageHandler implements MessageHandler {

    private final OrderMessageService orderMessageService;

    public OrderMessageHandler(OrderMessageService orderMessageService) {
        this.orderMessageService = orderMessageService;
    }

    @Override
    public void handleMessage(Message<?> message) {
        if (!(message.getPayload() instanceof OrderMessage order)) {
            throw new IllegalArgumentException("Unsupported message payload");
        }

        MessageHeaders headers = message.getHeaders();
        String source = headers.get("source", String.class);

        orderMessageService.process(order, source);
    }

}
