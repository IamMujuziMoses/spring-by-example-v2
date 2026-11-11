package com.springbyexample.v2.gateways.endpoint;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

import com.springbyexample.v2.gateways.model.OrderMessage;
import com.springbyexample.v2.gateways.model.OrderProcessingResult;
import com.springbyexample.v2.gateways.service.OrderMessageService;

/**
 * Processes order messages received from the gateway.
 *
 * @author Mujuzi Moses
 */
@MessageEndpoint
public class OrderMessageServiceActivator {

    private final OrderMessageService orderMessageService;

    public OrderMessageServiceActivator(OrderMessageService orderMessageService) {
        this.orderMessageService = orderMessageService;
    }

    @ServiceActivator(inputChannel = "orderInputChannel")
    public OrderProcessingResult process(OrderMessage order, @Header("source") String source) {
        return orderMessageService.process(order, source);
    }
}
