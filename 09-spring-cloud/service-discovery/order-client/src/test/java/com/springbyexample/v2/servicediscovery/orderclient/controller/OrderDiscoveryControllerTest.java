package com.springbyexample.v2.servicediscovery.orderclient.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springbyexample.v2.servicediscovery.orderclient.model.OrderResponse;
import com.springbyexample.v2.servicediscovery.orderclient.service.OrderDiscoveryService;

/**
 * Tests the Order Discovery Controller.
 *
 * @author Mujuzi Moses
 */
public class OrderDiscoveryControllerTest {

    private OrderDiscoveryService orderDiscoveryService;

    private OrderDiscoveryController orderDiscoveryController;

    @BeforeEach
    void setUp() {
        orderDiscoveryService = mock(OrderDiscoveryService.class);
        orderDiscoveryController = new OrderDiscoveryController(orderDiscoveryService);
    }

    @Test
    void getOrder_shouldReturnOrderFromDiscoveredService() {
        OrderResponse expected = new OrderResponse("order-123", "ACCEPTED", "order-service");

        when(orderDiscoveryService.getOrder("order-123")).thenReturn(expected);

        OrderResponse actual = orderDiscoveryController.getOrder("order-123");

        assertThat(actual).isEqualTo(expected);
    }
}

