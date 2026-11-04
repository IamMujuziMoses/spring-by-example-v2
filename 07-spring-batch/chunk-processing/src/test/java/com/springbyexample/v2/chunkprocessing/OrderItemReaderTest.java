package com.springbyexample.v2.chunkprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OrderItemReader}.
 *
 * @author Mujuzi Moses
 */
public class OrderItemReaderTest {

    @Test
    void read_shouldReturnItemsInOrder() throws Exception {
        OrderItemReader reader = new OrderItemReader(List.of("order-101", "order-102", "order-103"));

        assertThat(reader.read()).isEqualTo("order-101");
        assertThat(reader.read()).isEqualTo("order-102");
        assertThat(reader.read()).isEqualTo("order-103");
    }

    @Test
    void read_shouldReturnNullWhenItemsAreExhausted() throws Exception {
        OrderItemReader reader = new OrderItemReader(List.of("order-101"));

        reader.read();

        assertThat(reader.read()).isNull();
    }

    @Test
    void read_shouldReturnNullRepeatedlyAfterExhaustion() throws Exception {
        OrderItemReader reader = new OrderItemReader(List.of("order-101"));

        reader.read();

        assertThat(reader.read()).isNull();
        assertThat(reader.read()).isNull();
    }

    @Test
    void constructor_shouldRejectNullOrderIds() {
        assertThatNullPointerException().isThrownBy(() -> new OrderItemReader(null));
    }

    @Test
    void read_shouldReturnNullForEmptyInput() throws Exception {
        OrderItemReader reader = new OrderItemReader(List.of());

        assertThat(reader.read()).isNull();
    }
}
