package com.springbyexample.v2.serviceactivators.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.serviceactivators.model.OrderMessage;
import com.springbyexample.v2.serviceactivators.model.OrderProcessingResult;

/**
 * Unit tests for the order message service.
 *
 * @author Mujuzi Moses
 */
public class OrderMessageServiceTest {

    private final OrderMessageService service = new OrderMessageService();

    @Test
    void process_shouldCalculateOrderTotal() {
        OrderMessage order = new OrderMessage(1L, "Laptop", 2, new BigDecimal("1200.00"));

        OrderProcessingResult result = service.process(order);

        assertThat(result).isEqualTo(new OrderProcessingResult(1L, "Laptop", 2,
                new BigDecimal("2400.00")));
    }
}
