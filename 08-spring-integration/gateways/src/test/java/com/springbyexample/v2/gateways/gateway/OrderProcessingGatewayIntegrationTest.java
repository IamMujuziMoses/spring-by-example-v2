package com.springbyexample.v2.gateways.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.gateways.model.OrderMessage;
import com.springbyexample.v2.gateways.model.OrderProcessingResult;

/**
 * Integration tests for order processing through the messaging gateway.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class OrderProcessingGatewayIntegrationTest {

    @Autowired
    private OrderProcessingGateway orderProcessingGateway;

    @Test
    void processOrder_shouldReturnProcessingResult() {
        OrderMessage order = new OrderMessage(101L, "Laptop", 2, new BigDecimal("1200.00"));

        OrderProcessingResult result = orderProcessingGateway.process(order, "web");

        assertThat(result.orderId()).isEqualTo(101L);
        assertThat(result.product()).isEqualTo("Laptop");
        assertThat(result.quantity()).isEqualTo(2);
        assertThat(result.totalPrice()).isEqualByComparingTo("2400.00");
        assertThat(result.source()).isEqualTo("web");
    }

    @Test
    void processMultipleOrders_shouldReturnIndependentResults() {
        OrderMessage firstOrder = new OrderMessage(101L, "Laptop", 2, new BigDecimal("1200.00"));
        OrderMessage secondOrder = new OrderMessage(102L, "Keyboard", 3, new BigDecimal("50.00"));

        OrderProcessingResult firstResult = orderProcessingGateway.process(firstOrder, "web");
        OrderProcessingResult secondResult = orderProcessingGateway.process(secondOrder, "mobile");

        assertThat(firstResult.totalPrice()).isEqualByComparingTo("2400.00");
        assertThat(firstResult.source()).isEqualTo("web");
        assertThat(secondResult.totalPrice()).isEqualByComparingTo("150.00");
        assertThat(secondResult.source()).isEqualTo("mobile");
    }

    @Test
    void processOrder_shouldMapGatewayArgumentsToMessagePayloadAndHeaders() {
        OrderMessage order = new OrderMessage(103L, "Monitor", 1, new BigDecimal("350.00"));

        OrderProcessingResult result = orderProcessingGateway.process(order, "mobile");

        assertThat(result.orderId()).isEqualTo(order.orderId());
        assertThat(result.source()).isEqualTo("mobile");
    }
}
