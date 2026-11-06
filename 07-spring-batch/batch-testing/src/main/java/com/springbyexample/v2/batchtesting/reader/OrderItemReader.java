package com.springbyexample.v2.batchtesting.reader;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.springframework.batch.infrastructure.item.ItemReader;

import com.springbyexample.v2.batchtesting.domain.Order;

/**
 * Reads orders from an in-memory collection.
 *
 * @author Mujuzi Moses
 */
public class OrderItemReader implements ItemReader<Order> {

    private final Iterator<Order> iterator;

    public OrderItemReader(List<Order> orders) {
        this.iterator = Objects.requireNonNull(orders, "Orders must not be null").iterator();
    }

    @Override
    public Order read() {
        return iterator.hasNext() ? iterator.next() : null;
    }
}
