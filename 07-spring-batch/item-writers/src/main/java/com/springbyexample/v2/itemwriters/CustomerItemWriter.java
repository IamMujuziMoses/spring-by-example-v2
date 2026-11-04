package com.springbyexample.v2.itemwriters;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

/**
 * Writes customer names received in a chunk to a configured output destination.
 *
 * @author Mujuzi Moses
 */
public class CustomerItemWriter implements ItemWriter<String> {

    private final Consumer<List<String>> output;

    public CustomerItemWriter(Consumer<List<String>> output) {
        this.output = Objects.requireNonNull(output, "Output consumer must not be null");
    }

    @Override
    public void write(Chunk<? extends String> chunk) {
        output.accept(List.copyOf(chunk.getItems()));
    }
}
