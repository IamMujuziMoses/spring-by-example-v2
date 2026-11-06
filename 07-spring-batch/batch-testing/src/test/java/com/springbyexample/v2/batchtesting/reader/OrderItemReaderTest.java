package com.springbyexample.v2.batchtesting.reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.springbyexample.v2.batchtesting.domain.Order;

/**
 * Unit tests for {@link OrderItemReader}.
 *
 * @author Mujuzi Moses
 */
public class OrderItemReaderTest {

    @Test
    void read_shouldReturnOrdersInInputOrder() {
        Order first = new Order(1L, "first order");
        Order second = new Order(2L, "second order");

        OrderItemReader reader = new OrderItemReader(List.of(first, second));

        assertThat(reader.read()).isEqualTo(first);
        assertThat(reader.read()).isEqualTo(second);
    }

    @Test
    void read_shouldReturnNullWhenAllOrdersHaveBeenRead() {
        OrderItemReader reader = new OrderItemReader(List.of(new Order(1L, "first order")));

        reader.read();

        assertThat(reader.read()).isNull();
    }

    @Test
    void constructor_shouldRejectNullOrders() {
        assertThatNullPointerException().isThrownBy(() -> new OrderItemReader(null))
                .withMessage("Orders must not be null");
    }
}
