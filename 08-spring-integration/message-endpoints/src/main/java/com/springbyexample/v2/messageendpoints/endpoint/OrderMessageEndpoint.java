package com.springbyexample.v2.messageendpoints.endpoint;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;

import com.springbyexample.v2.messageendpoints.domain.OrderMessage;
import com.springbyexample.v2.messageendpoints.service.OrderMessageService;

/**
 * Connects the order endpoint channel to the order message service.
 *
 * @author Mujuzi Moses
 */
@MessageEndpoint
public class OrderMessageEndpoint {

    private final OrderMessageService orderMessageService;

    public OrderMessageEndpoint(OrderMessageService orderMessageService) {
        this.orderMessageService = orderMessageService;
    }

    @ServiceActivator(inputChannel = "orderEndpointChannel")
    public void handleOrder(OrderMessage order) {
        orderMessageService.processOrder(order);
    }
}
