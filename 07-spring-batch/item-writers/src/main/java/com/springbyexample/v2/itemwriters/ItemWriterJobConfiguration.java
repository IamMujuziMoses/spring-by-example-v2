package com.springbyexample.v2.itemwriters;

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
 * Configures the Spring Batch item writers example.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class ItemWriterJobConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemWriterJobConfiguration.class);

    private static final int CHUNK_SIZE = 2;

    private static final List<String> CUSTOMERS = List.of(" Alice ", "Bob", "   ", "Charlie", " Diana ");

    @Bean
    @StepScope
    public CustomerItemReader customerItemReader() {
        return new CustomerItemReader(CUSTOMERS);
    }

    @Bean
    public CustomerItemProcessor customerItemProcessor() {
        return new CustomerItemProcessor();
    }

    @Bean
    public CustomerItemWriter customerItemWriter() {
        return new CustomerItemWriter(customerNames -> LOGGER.info("Writing customer names: {}", customerNames));
    }

    @Bean
    public Step writeCustomersStep(JobRepository jobRepository, ItemReader<String> customerItemReader,
            ItemProcessor<String, String> customerItemProcessor, ItemWriter<String> customerItemWriter) {

        return new ChunkOrientedStepBuilder<String, String>("writeCustomersStep", jobRepository, CHUNK_SIZE)
                .reader(customerItemReader)
                .processor(customerItemProcessor)
                .writer(customerItemWriter)
                .build();
    }

    @Bean
    public Job writeCustomersJob(JobRepository jobRepository, Step writeCustomersStep) {

        return new JobBuilder("writeCustomersJob", jobRepository).start(writeCustomersStep).build();
    }
}
