package com.springbyexample.v2.batchtesting.writer;

import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.springbyexample.v2.batchtesting.domain.ProcessedOrder;
import com.springbyexample.v2.batchtesting.support.BatchTestingSupport;

/**
 * Records processed orders.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderItemWriter implements ItemWriter<ProcessedOrder> {

    private final BatchTestingSupport support;

    public OrderItemWriter(BatchTestingSupport support) {
        this.support = support;
    }

    @Override
    public void write(Chunk<? extends ProcessedOrder> chunk) {
        support.recordWrittenOrders(chunk.getItems());
    }
}
