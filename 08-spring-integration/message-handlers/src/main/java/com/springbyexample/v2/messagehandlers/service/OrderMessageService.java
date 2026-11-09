package com.springbyexample.v2.messagehandlers.service;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.messagehandlers.model.OrderMessage;
import com.springbyexample.v2.messagehandlers.support.MessageHandlerSupport;

/**
 * Processes order messages received by the message handler.
 *
 * @author Mujuzi Moses
 */
@Service
public class OrderMessageService {

    private final MessageHandlerSupport support;

    public OrderMessageService(MessageHandlerSupport support) {
        this.support = support;
    }

    public void process(OrderMessage order, String source) {
        support.recordProcessedOrder(order, source);
    }

}
