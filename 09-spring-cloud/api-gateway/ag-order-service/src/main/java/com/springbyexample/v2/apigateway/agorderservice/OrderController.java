package com.springbyexample.v2.apigateway.agorderservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing order-related endpoints.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable String orderId) {
        return new OrderResponse("order-service", orderId, "Order retrieved successfully.");
    }

}
