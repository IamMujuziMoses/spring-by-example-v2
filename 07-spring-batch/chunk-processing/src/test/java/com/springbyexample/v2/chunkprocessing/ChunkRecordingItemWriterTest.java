package com.springbyexample.v2.chunkprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;

/**
 * Tests for {@link ChunkRecordingItemWriter}.
 *
 * @author Mujuzi Moses
 */
public class ChunkRecordingItemWriterTest {

    private final ChunkRecordingItemWriter writer = new ChunkRecordingItemWriter();

    @Test
    void write_shouldRecordChunkItems() throws Exception {
        Chunk<String> chunk = new Chunk<>(List.of("ORDER-101", "ORDER-102", "ORDER-103"));

        writer.write(chunk);

        assertThat(writer.getWrittenChunks()).containsExactly(List.of("ORDER-101", "ORDER-102", "ORDER-103"));
    }

    @Test
    void write_shouldRecordMultipleChunksSeparately() throws Exception {
        Chunk<String> firstChunk = new Chunk<>(List.of("ORDER-101", "ORDER-102", "ORDER-103"));
        Chunk<String> secondChunk = new Chunk<>(List.of("ORDER-104", "ORDER-105"));

        writer.write(firstChunk);
        writer.write(secondChunk);

        assertThat(writer.getWrittenChunks()).containsExactly(List.of("ORDER-101", "ORDER-102", "ORDER-103"),
                List.of("ORDER-104", "ORDER-105"));
    }

    @Test
    void write_shouldRecordEmptyChunk() throws Exception {
        writer.write(new Chunk<>());

        assertThat(writer.getWrittenChunks()).containsExactly(List.of());
    }

    @Test
    void getWrittenChunks_shouldReturnImmutableCopies() throws Exception {
        writer.write(new Chunk<>(List.of("ORDER-101", "ORDER-102")));

        List<List<String>> writtenChunks = writer.getWrittenChunks();

        assertThatThrownBy(() -> writtenChunks.add(List.of("ORDER-103")))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> writtenChunks.getFirst().add("ORDER-103"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
