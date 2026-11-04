package com.springbyexample.v2.itemwriters;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.springframework.batch.infrastructure.item.ItemReader;

/**
 * Reads customer names from an in-memory collection.
 *
 * @author Mujuzi Moses
 */
public class CustomerItemReader implements ItemReader<String> {

    private final Iterator<String> customerNames;

    public CustomerItemReader(List<String> customerNames) {
        this.customerNames = List.copyOf(
                Objects.requireNonNull(customerNames, "Customer names must not be null")
        ).iterator();
    }

    @Override
    public String read() {
        return customerNames.hasNext() ? customerNames.next() : null;
    }
}
