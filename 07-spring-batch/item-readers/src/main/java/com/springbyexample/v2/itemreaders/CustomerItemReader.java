package com.springbyexample.v2.itemreaders;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.springframework.batch.infrastructure.item.ItemReader;

/**
 * Reads customer names sequentially from an in-memory collection.
 *
 * @author Mujuzi Moses
 */
public class CustomerItemReader implements ItemReader<String> {

    private final Iterator<String> customers;

    /**
     * Creates a reader for the supplied customer names.
     *
     * @param customerNames customer names to read
     */
    public CustomerItemReader(List<String> customerNames) {
        Objects.requireNonNull(customerNames, "Customer names must not be null");
        this.customers = List.copyOf(customerNames).iterator();
    }

    /**
     * Reads the next customer name.
     *
     * @return the next customer name, or {@code null} when no more items exist
     */
    @Override
    public String read() {
        if (customers.hasNext()) {
            return customers.next();
        }

        return null;
    }
}
