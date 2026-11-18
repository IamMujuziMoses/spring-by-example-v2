package com.springbyexample.v2.resilience.orderclient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/client/orders")
public class OrderClientController {

    private final OrderServiceClient orderServiceClient;

    public OrderClientController(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable("orderId") String orderId) {
        return orderServiceClient.getOrder(orderId);
    }

}