package com.springbyexample.v2.configurationmanagement.cmorderservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the effective Order Service configuration.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderProperties orderProperties;

    public OrderController(OrderProperties orderProperties) {
        this.orderProperties = orderProperties;
    }

    @GetMapping("/configuration")
    public OrderConfigurationResponse getConfiguration() {
        return new OrderConfigurationResponse(orderProperties.message(), orderProperties.maxItems(),
                orderProperties.currency(), orderProperties.expeditedEnabled());
    }
}
