package com.springbyexample.v2.gateways.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.gateways.model.OrderMessage;
import com.springbyexample.v2.gateways.model.OrderProcessingResult;

/**
 * Unit tests for order message processing.
 *
 * @author Mujuzi Moses
 */
public class OrderMessageServiceTest {

    private final OrderMessageService orderMessageService = new OrderMessageService();

    @Test
    void process_shouldCalculateOrderTotal() {
        OrderMessage order = new OrderMessage(101L, "Laptop", 2, new BigDecimal("1200.00"));

        OrderProcessingResult result = orderMessageService.process(order, "web");

        assertThat(result.totalPrice()).isEqualByComparingTo("2400.00");
        assertThat(result.source()).isEqualTo("web");
    }
}
