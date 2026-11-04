package com.springbyexample.v2.itemwriters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;

/**
 * Unit tests for {@link CustomerItemWriter}.
 *
 * @author Mujuzi Moses
 */
public class CustomerItemWriterTest {

    private List<List<String>> writtenChunks;

    private CustomerItemWriter writer;

    @BeforeEach
    void setUp() {
        writtenChunks = new ArrayList<>();
        writer = new CustomerItemWriter(writtenChunks::add);
    }

    @Test
    void write_shouldPassChunkItemsToOutput() throws Exception {
        Chunk<String> chunk = new Chunk<>(List.of("ALICE", "BOB"));

        writer.write(chunk);

        assertThat(writtenChunks).containsExactly(List.of("ALICE", "BOB"));
    }

    @Test
    void write_shouldHandleMultipleChunks() throws Exception {
        writer.write(new Chunk<>(List.of("ALICE", "BOB")));
        writer.write(new Chunk<>(List.of("CHARLIE")));

        assertThat(writtenChunks).containsExactly(List.of("ALICE", "BOB"), List.of("CHARLIE"));
    }

    @Test
    void write_shouldHandleEmptyChunk() throws Exception {
        writer.write(new Chunk<>());

        assertThat(writtenChunks).containsExactly(List.of());
    }

    @Test
    void constructor_shouldRejectNullOutput() {
        assertThatNullPointerException().isThrownBy(() -> new CustomerItemWriter(null))
                .withMessage("Output consumer must not be null");
    }

    @Test
    void write_shouldPassImmutableCopyOfChunkItems() throws Exception {
        writer.write(new Chunk<>(List.of("ALICE", "BOB")));

        assertThatThrownBy(() -> writtenChunks.getFirst().add("CHARLIE")).isInstanceOf(UnsupportedOperationException.class);
    }
}
