package com.springbyexample.v2.skipandretry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springbyexample.v2.skipandretry.exception.InvalidOrderException;
import com.springbyexample.v2.skipandretry.exception.TransientOrderProcessingException;

/**
 * Unit tests for order processing.
 *
 * @author Mujuzi Moses
 */
public class OrderItemProcessorTest {

    private SkipAndRetrySupport support;

    private OrderItemProcessor processor;

    @BeforeEach
    void setUp() {
        support = new SkipAndRetrySupport();
        processor = new OrderItemProcessor(support);
    }

    @Test
    void process_shouldTransformValidOrder() {
        CustomerOrder order = new CustomerOrder(1L, "customer order", false, 0);

        ProcessedOrder result = processor.process(order);

        assertEquals(new ProcessedOrder(1L, "CUSTOMER ORDER"), result);
    }

    @Test
    void process_shouldThrowExceptionForInvalidOrder() {
        CustomerOrder order = new CustomerOrder(1L, "invalid order", true, 0);

        assertThrows(InvalidOrderException.class, () -> processor.process(order));
    }

    @Test
    void process_shouldThrowTransientExceptionBeforeConfiguredAttempts() {
        CustomerOrder order = new CustomerOrder(1L, "customer order", false, 2);

        assertThrows(TransientOrderProcessingException.class, () -> processor.process(order));
        assertThrows(TransientOrderProcessingException.class, () -> processor.process(order));
        assertEquals(2, support.processingAttemptsFor(1L));
    }

    @Test
    void process_shouldSucceedAfterTransientFailures() {
        CustomerOrder order = new CustomerOrder(1L, "customer order", false, 2);

        assertThrows(TransientOrderProcessingException.class, () -> processor.process(order));
        assertThrows(TransientOrderProcessingException.class, () -> processor.process(order));

        ProcessedOrder result = processor.process(order);

        assertEquals(new ProcessedOrder(1L, "CUSTOMER ORDER"), result);
        assertEquals(3, support.processingAttemptsFor(1L));
    }
}
