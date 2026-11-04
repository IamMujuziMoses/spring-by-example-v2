package com.springbyexample.v2.chunkprocessing;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.springframework.batch.infrastructure.item.ItemReader;

/**
 * Reads order identifiers from an in-memory collection.
 *
 * @author Mujuzi Moses
 */
public class OrderItemReader implements ItemReader<String> {

    private final Iterator<String> orderIds;

    public OrderItemReader(List<String> orderIds) {
        this.orderIds = List.copyOf(Objects.requireNonNull(orderIds, "Order IDs must not be null")).iterator();
    }

    @Override
    public String read() {
        return orderIds.hasNext() ? orderIds.next() : null;
    }
}
