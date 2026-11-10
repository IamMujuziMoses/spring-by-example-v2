package com.springbyexample.v2.serviceactivators.service;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import com.springbyexample.v2.serviceactivators.model.OrderMessage;
import com.springbyexample.v2.serviceactivators.model.OrderProcessingResult;

/**
 * Connects the order input channel to the order processing service.
 *
 * @author Mujuzi Moses
 */
@MessageEndpoint
public class OrderMessageServiceActivator {

    private final OrderMessageService orderMessageService;

    public OrderMessageServiceActivator(OrderMessageService orderMessageService) {
        this.orderMessageService = orderMessageService;
    }

    @ServiceActivator(inputChannel = "orderInputChannel", outputChannel = "orderOutputChannel")
    public OrderProcessingResult process(OrderMessage order) {
        return orderMessageService.process(order);
    }
}
