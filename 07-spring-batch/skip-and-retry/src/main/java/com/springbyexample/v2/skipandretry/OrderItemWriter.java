package com.springbyexample.v2.skipandretry;

import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * Records successfully processed orders.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderItemWriter implements ItemWriter<ProcessedOrder> {

    private final SkipAndRetrySupport support;

    public OrderItemWriter(SkipAndRetrySupport support) {
        this.support = support;
    }

    @Override
    public void write(Chunk<? extends ProcessedOrder> chunk) {
        support.recordWrittenOrders(chunk.getItems());
    }
}
