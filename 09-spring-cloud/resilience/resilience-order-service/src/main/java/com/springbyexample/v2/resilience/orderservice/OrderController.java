package com.springbyexample.v2.resilience.orderservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final FailureModeService failureModeService;

    private final String instanceId;

    public OrderController(FailureModeService failureModeService,
                           @Value("${app.instance-id:order-service}") String instanceId) {
        this.failureModeService = failureModeService;
        this.instanceId = instanceId;
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable("orderId") String orderId) {
        if (failureModeService.shouldFail()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Order service is temporarily unavailable");
        }

        return new OrderResponse(orderId, "PROCESSED", instanceId, false, null);
    }

}
