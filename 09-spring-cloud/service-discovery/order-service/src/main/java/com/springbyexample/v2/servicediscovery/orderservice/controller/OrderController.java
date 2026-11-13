package com.springbyexample.v2.servicediscovery.orderservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.servicediscovery.orderservice.model.OrderResponse;

/**
 * Exposes order-related HTTP endpoints.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    /**
     * Retrieves an order by its identifier.
     *
     * @param orderId order identifier
     * @return order response
     */
    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable String orderId) {
        return new OrderResponse(orderId, "ACCEPTED", "order-service");
    }
}
