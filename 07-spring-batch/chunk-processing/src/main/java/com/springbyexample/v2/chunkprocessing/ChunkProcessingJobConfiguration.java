package com.springbyexample.v2.chunkprocessing;

import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Spring Batch chunk processing example.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class ChunkProcessingJobConfiguration {

    private static final int CHUNK_SIZE = 3;

    private static final List<String> ORDER_IDS = List.of("order-101", "order-102", "order-103", "order-104",
            "order-105", "order-106", "order-107");

    @Bean
    @StepScope
    public OrderItemReader orderItemReader() {
        return new OrderItemReader(ORDER_IDS);
    }

    @Bean
    public OrderItemProcessor orderItemProcessor() {
        return new OrderItemProcessor();
    }

    @Bean
    public ChunkRecordingItemWriter chunkRecordingItemWriter() {
        return new ChunkRecordingItemWriter();
    }

    @Bean
    public Step processOrdersStep(JobRepository jobRepository, ItemReader<String> orderItemReader,
            ItemProcessor<String, String> orderItemProcessor, ItemWriter<String> chunkRecordingItemWriter) {

        return new ChunkOrientedStepBuilder<String, String>("processOrdersStep", jobRepository, CHUNK_SIZE)
                .reader(orderItemReader)
                .processor(orderItemProcessor)
                .writer(chunkRecordingItemWriter)
                .build();
    }

    @Bean
    public Job chunkProcessingJob(JobRepository jobRepository, Step processOrdersStep) {
        return new JobBuilder("chunkProcessingJob", jobRepository).start(processOrdersStep).build();
    }
}
