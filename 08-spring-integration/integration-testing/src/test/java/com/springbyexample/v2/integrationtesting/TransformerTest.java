package com.springbyexample.v2.integrationtesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springbyexample.v2.integrationtesting.model.OrderMessage;
import com.springbyexample.v2.integrationtesting.model.OrderSummary;
import com.springbyexample.v2.integrationtesting.service.OrderTransformer;

/**
 * Unit tests for the order transformer.
 *
 * @author Mujuzi Moses
 */
public class TransformerTest {

    private OrderTransformer orderTransformer;

    @BeforeEach
    void setUp() {
        orderTransformer = new OrderTransformer();
    }

    /**
     * Verifies that an order message is transformed into an order summary.
     */
    @Test
    void transform_shouldConvertOrderMessageIntoOrderSummary() {
        OrderMessage order = new OrderMessage("ORDER-001", 3);
        OrderSummary summary = orderTransformer.transform(order);

        assertEquals("ORDER-001", summary.orderId());
        assertEquals(3, summary.quantity());
        assertEquals("Order containing 3 items", summary.description());
    }

    /**
     * Verifies that unsupported payloads are rejected.
     */
    @Test
    void transform_shouldThrowIllegalArgumentExceptionForUnsupportedPayload() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderTransformer.transform("INVALID"));

        assertEquals("Unsupported message payload", exception.getMessage());
    }
}
