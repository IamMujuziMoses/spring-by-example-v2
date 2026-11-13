package com.springbyexample.v2.servicediscovery.orderclient.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.servicediscovery.orderclient.model.OrderResponse;
import com.springbyexample.v2.servicediscovery.orderclient.service.OrderDiscoveryService;

/**
 * Exposes endpoints for discovering and invoking the Order Service.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/orders")
public class OrderDiscoveryController {

    private final OrderDiscoveryService orderDiscoveryService;

    /**
     * Creates an Order Discovery Controller.
     *
     * @param orderDiscoveryService order discovery service
     */
    public OrderDiscoveryController(OrderDiscoveryService orderDiscoveryService) {
        this.orderDiscoveryService = orderDiscoveryService;
    }

    /**
     * Retrieves an order through the discovered Order Service.
     *
     * @param orderId order identifier
     * @return order response
     */
    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable String orderId) {
        return orderDiscoveryService.getOrder(orderId);
    }
}
