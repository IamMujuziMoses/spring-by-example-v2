package com.springbyexample.v2.itemprocessors;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Configures the Spring Batch item processor example.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class ItemProcessorJobConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemProcessorJobConfiguration.class);

    private static final int CHUNK_SIZE = 2;

    private static final List<String> CUSTOMERS = List.of(" Alice ", "Bob", "   ", "Charlie", " Diana ");

    /**
     * Creates the customer item reader.
     *
     * @return customer item reader
     */
    @Bean
    @StepScope
    public ItemReader<String> customerItemReader() {
        return new CustomerItemReader(CUSTOMERS);
    }

    /**
     * Creates the customer item processor.
     *
     * @return customer item processor
     */
    @Bean
    public ItemProcessor<String, String> customerItemProcessor() {
        return new CustomerItemProcessor();
    }

    /**
     * Creates the customer item writer.
     *
     * @return customer item writer
     */
    @Bean
    public ItemWriter<String> customerItemWriter() {
        return chunk -> LOGGER.info("Writing processed customer names: {}", chunk.getItems());
    }

    /**
     * Creates the step that reads, processes, and writes customer names.
     *
     * @param jobRepository         Spring Batch job repository
     * @param customerItemReader    customer item reader
     * @param customerItemProcessor customer item processor
     * @param customerItemWriter    customer item writer
     * @return configured customer processing step
     */
    @Bean
    public Step processCustomersStep(JobRepository jobRepository, ItemReader<String> customerItemReader,
            ItemProcessor<String, String> customerItemProcessor, ItemWriter<String> customerItemWriter) {

        return new ChunkOrientedStepBuilder<String, String>("processCustomersStep", jobRepository, CHUNK_SIZE)
                .reader(customerItemReader)
                .processor(customerItemProcessor)
                .writer(customerItemWriter)
                .build();
    }

    /**
     * Creates the customer processing job.
     *
     * @param jobRepository        Spring Batch job repository
     * @param processCustomersStep customer processing step
     * @return configured customer processing job
     */
    @Bean
    public Job processCustomersJob(JobRepository jobRepository, Step processCustomersStep) {

        return new JobBuilder("processCustomersJob", jobRepository).start(processCustomersStep).build();
    }
}
