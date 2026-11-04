package com.springbyexample.v2.chunkprocessing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

/**
 * Records the items received in each chunk.
 *
 * @author Mujuzi Moses
 */
public class ChunkRecordingItemWriter implements ItemWriter<String> {

    private final List<List<String>> writtenChunks = new ArrayList<>();

    @Override
    public void write(Chunk<? extends String> chunk) {
        writtenChunks.add(List.copyOf(chunk.getItems()));
    }

    public List<List<String>> getWrittenChunks() {
        return List.copyOf(writtenChunks);
    }
}
