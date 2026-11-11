package com.springbyexample.v2.gateways.gateway;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;

import com.springbyexample.v2.gateways.model.OrderMessage;
import com.springbyexample.v2.gateways.model.OrderProcessingResult;

/**
 * Gateway for submitting orders to the Spring Integration flow.
 *
 * @author Mujuzi Moses
 */
@MessagingGateway(defaultRequestChannel = "orderInputChannel")
public interface OrderProcessingGateway {

    OrderProcessingResult process(OrderMessage order, @Header("source") String source);
}
