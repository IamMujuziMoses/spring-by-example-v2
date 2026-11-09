package com.springbyexample.v2.messageendpoints.service;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.messageendpoints.domain.OrderMessage;
import com.springbyexample.v2.messageendpoints.support.MessageEndpointSupport;

/**
 * Provides business operations for processing order messages.
 *
 * @author Mujuzi Moses
 */
@Service
public class OrderMessageService {

    private final MessageEndpointSupport support;

    public OrderMessageService(MessageEndpointSupport support) {
        this.support = support;
    }

    public void processOrder(OrderMessage order) {
        support.recordProcessedOrder(order);
    }
}
