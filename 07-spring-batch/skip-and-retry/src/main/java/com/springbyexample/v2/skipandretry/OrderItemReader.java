package com.springbyexample.v2.skipandretry;

import java.util.Iterator;
import java.util.List;

import org.springframework.batch.infrastructure.item.ItemReader;

/**
 * Reads customer orders from an in-memory collection.
 *
 * @author Mujuzi Moses
 */
public class OrderItemReader implements ItemReader<CustomerOrder> {

    private final Iterator<CustomerOrder> orders;

    public OrderItemReader(List<CustomerOrder> orders) {
        this.orders = List.copyOf(orders).iterator();
    }

    @Override
    public CustomerOrder read() {
        return orders.hasNext() ? orders.next() : null;
    }
}
