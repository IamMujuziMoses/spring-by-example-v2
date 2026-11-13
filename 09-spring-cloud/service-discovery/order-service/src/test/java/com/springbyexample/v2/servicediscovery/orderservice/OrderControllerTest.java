package com.springbyexample.v2.servicediscovery.orderservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.servicediscovery.orderservice.controller.OrderController;
import com.springbyexample.v2.servicediscovery.orderservice.model.OrderResponse;

/**
 * Tests the Order Controller.
 *
 * @author Mujuzi Moses
 */
public class OrderControllerTest {

    private final OrderController orderController = new OrderController();

    @Test
    void getOrder_shouldReturnOrderWhenOrderIdIsProvided() {
        OrderResponse response = orderController.getOrder("order-123");

        assertThat(response.orderId()).isEqualTo("order-123");
        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.service()).isEqualTo("order-service");
    }
}
