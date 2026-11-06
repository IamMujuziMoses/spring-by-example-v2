package com.springbyexample.v2.batchtesting.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.batchtesting.domain.Order;
import com.springbyexample.v2.batchtesting.domain.ProcessedOrder;

/**
 * Unit tests for {@link OrderItemProcessor}.
 *
 * @author Mujuzi Moses
 */
public class OrderItemProcessorTest {

    private final OrderItemProcessor processor = new OrderItemProcessor();

    @Test
    void process_shouldTransformOrderDescriptionToUppercase() {
        Order order = new Order(1L, "first order");

        ProcessedOrder result = processor.process(order);

        assertThat(result).isEqualTo(new ProcessedOrder(1L, "FIRST ORDER"));
    }

    @Test
    void process_shouldPreserveOrderIdentifier() {
        Order order = new Order(42L, "sample order");

        ProcessedOrder result = processor.process(order);

        assertNotNull(result);
        assertThat(result.orderId()).isEqualTo(42L);
    }

    @Test
    void process_shouldRejectNullOrder() {
        assertThatNullPointerException().isThrownBy(() -> processor.process(null))
                .withMessage("Order must not be null");
    }
}
