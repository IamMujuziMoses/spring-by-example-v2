package com.springbyexample.v2.errorchannels.service;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

import com.springbyexample.v2.errorchannels.model.OrderMessage;

/**
 * Processes incoming orders and rejects invalid orders.
 *
 * @author Mujuzi Moses
 */
@Service
public class OrderProcessingService {

    /**
     * Processes an order received through the asynchronous input channel.
     *
     * @param order order to process
     */
    @ServiceActivator(inputChannel = "orderInputChannel")
    public void process(OrderMessage order) {
        if (order.quantity() <= 0) {
            throw new IllegalArgumentException("Order quantity must be greater than zero");
        }
    }
}
