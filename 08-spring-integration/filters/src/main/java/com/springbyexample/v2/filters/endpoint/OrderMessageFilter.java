package com.springbyexample.v2.filters.endpoint;

import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.MessageEndpoint;

import com.springbyexample.v2.filters.model.OrderMessage;

/**
 * Filters order messages based on their quantity.
 *
 * @author Mujuzi Moses
 */
@MessageEndpoint
public class OrderMessageFilter {

    @Filter(inputChannel = "orderInputChannel", outputChannel = "orderOutputChannel")
    public boolean accept(OrderMessage order) {
        return order.quantity() > 0;
    }
}
