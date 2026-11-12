package com.springbyexample.v2.integrationtesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springbyexample.v2.integrationtesting.model.OrderSummary;
import com.springbyexample.v2.integrationtesting.service.OrderProcessingService;

/**
 * Unit tests for the order processing service.
 *
 * @author Mujuzi Moses
 */
public class ServiceActivatorTest {

    private OrderProcessingService orderProcessingService;

    @BeforeEach
    void setUp() {
        orderProcessingService = new OrderProcessingService();
    }

    /**
     * Verifies that an order summary is processed successfully.
     */
    @Test
    void process_shouldReturnProcessedOrderSummary() {
        OrderSummary order = new OrderSummary("ORDER-001", 3, "Order containing 3 items");

        OrderSummary result = orderProcessingService.process(order);

        assertEquals("ORDER-001", result.orderId());
        assertEquals(3, result.quantity());
        assertEquals("Processed: Order containing 3 items", result.description());
    }

    /**
     * Verifies that orders with non-positive quantities are rejected.
     */
    @Test
    void process_shouldThrowIllegalArgumentExceptionForInvalidQuantity() {
        OrderSummary order = new OrderSummary("ORDER-001", 0, "Order containing 0 items");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderProcessingService.process(order));

        assertEquals("Order quantity must be greater than zero", exception.getMessage());
    }
}
