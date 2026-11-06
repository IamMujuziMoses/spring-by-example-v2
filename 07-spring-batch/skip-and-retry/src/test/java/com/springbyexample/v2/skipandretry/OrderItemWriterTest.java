package com.springbyexample.v2.skipandretry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;

/**
 * Unit tests for the order item writer.
 *
 * @author Mujuzi Moses
 */
public class OrderItemWriterTest {

    private SkipAndRetrySupport support;

    private OrderItemWriter writer;

    @BeforeEach
    void setUp() {
        support = new SkipAndRetrySupport();
        writer = new OrderItemWriter(support);
    }

    @Test
    void write_shouldRecordAllOrdersInChunk() {
        Chunk<ProcessedOrder> chunk = Chunk.of(
                new ProcessedOrder(1L, "ORDER 1"),
                new ProcessedOrder(2L, "ORDER 2"));

        writer.write(chunk);

        assertEquals(
                List.of(
                        new ProcessedOrder(1L, "ORDER 1"),
                        new ProcessedOrder(2L, "ORDER 2")
                ),
                support.writtenOrders()
        );
    }

    @Test
    void write_shouldAppendOrdersAcrossChunks() {
        writer.write(Chunk.of(new ProcessedOrder(1L, "ORDER 1")));
        writer.write(Chunk.of(new ProcessedOrder(2L, "ORDER 2")));

        assertEquals(2, support.writtenOrders().size());
    }

    @Test
    void write_shouldHandleEmptyChunk() {
        writer.write(new Chunk<>());

        assertEquals(List.of(), support.writtenOrders());
    }
}
