package com.springbyexample.v2.loadbalancing.lborderservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes order-related endpoints.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final String instanceId;

    public OrderController(@Value("${app.instance-id:order-service}") String instanceId) {
        this.instanceId = instanceId;
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable("orderId") String orderId) {
        return new OrderResponse(orderId, instanceId);
    }

}
