package com.springbyexample.v2.itemreaders;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures a Spring Batch job that reads customer names using an
 * ItemReader.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class ItemReaderJobConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemReaderJobConfiguration.class);

    private static final int CHUNK_SIZE = 2;

    private static final List<String> CUSTOMERS = List.of("Alice", "Bob", "Charlie", "Diana", "Edward");

    /**
     * Creates a step-scoped reader that reads customer names.
     *
     * @return the customer item reader
     */
    @Bean
    @StepScope
    public ItemReader<String> customerItemReader() {
        return new CustomerItemReader(CUSTOMERS);
    }

    /**
     * Creates a writer that logs the customer names received in each chunk.
     *
     * @return the customer item writer
     */
    @Bean
    public ItemWriter<String> customerItemWriter() {
        return chunk -> LOGGER.info("Writing customer names: {}", chunk.getItems());
    }

    /**
     * Creates a chunk-oriented step that reads customer names and passes
     * them to the writer.
     *
     * @param jobRepository      repository used to manage batch metadata
     * @param customerItemReader reader that supplies customer names
     * @param customerItemWriter writer that receives the customer names
     * @return the customer reading step
     */
    @Bean
    public Step readCustomersStep(JobRepository jobRepository, ItemReader<String> customerItemReader,
            ItemWriter<String> customerItemWriter) {

        return new ChunkOrientedStepBuilder<String, String>("readCustomersStep", jobRepository, CHUNK_SIZE)
                .reader(customerItemReader)
                .writer(customerItemWriter)
                .build();
    }

    /**
     * Creates a job that reads customer names.
     *
     * @param jobRepository     repository used to manage batch metadata
     * @param readCustomersStep step that reads customer names
     * @return the customer reading job
     */
    @Bean
    public Job readCustomersJob(JobRepository jobRepository, Step readCustomersStep) {

        return new JobBuilder("readCustomersJob", jobRepository).start(readCustomersStep).build();
    }

}
