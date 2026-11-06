package com.springbyexample.v2.batchtesting.writer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;

import com.springbyexample.v2.batchtesting.domain.ProcessedOrder;
import com.springbyexample.v2.batchtesting.support.BatchTestingSupport;

/**
 * Unit tests for {@link OrderItemWriter}.
 *
 * @author Mujuzi Moses
 */
public class OrderItemWriterTest {

    private BatchTestingSupport support;

    private OrderItemWriter writer;

    @BeforeEach
    void setUp() {
        support = new BatchTestingSupport();
        writer = new OrderItemWriter(support);
    }

    @Test
    void write_shouldRecordProcessedOrders() {
        Chunk<ProcessedOrder> chunk = new Chunk<>(List.of(
                new ProcessedOrder(1L, "FIRST ORDER"),
                new ProcessedOrder(2L, "SECOND ORDER")));

        writer.write(chunk);

        assertThat(support.writtenOrders()).hasSize(2);
        assertThat(support.writtenOrders())
                .containsExactly(
                        new ProcessedOrder(1L, "FIRST ORDER"),
                        new ProcessedOrder(2L, "SECOND ORDER"));
    }

    @Test
    void write_shouldAccumulateOrdersAcrossChunks() {
        writer.write(new Chunk<>(List.of(new ProcessedOrder(1L, "FIRST ORDER"))));

        writer.write(new Chunk<>(List.of(new ProcessedOrder(2L, "SECOND ORDER"))));

        assertThat(support.writtenOrders()).hasSize(2);
    }

    @Test
    void write_shouldNotAddOrdersWhenChunkIsEmpty() {
        writer.write(new Chunk<>());

        assertThat(support.writtenOrders()).isEmpty();
    }
}
