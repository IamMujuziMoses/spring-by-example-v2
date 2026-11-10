package com.springbyexample.v2.routers.router;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Router;

import com.springbyexample.v2.routers.model.OrderMessage;

/**
 * Routes order messages to channels based on their order type.
 *
 * @author Mujuzi Moses
 */
@MessageEndpoint
public class OrderMessageRouter {

    @Router(inputChannel = "orderInputChannel")
    public String route(OrderMessage order) {
        return switch (order.orderType()) {

            case RETAIL -> "retailOrderChannel";

            case WHOLESALE -> "wholesaleOrderChannel";
        };
    }
}
