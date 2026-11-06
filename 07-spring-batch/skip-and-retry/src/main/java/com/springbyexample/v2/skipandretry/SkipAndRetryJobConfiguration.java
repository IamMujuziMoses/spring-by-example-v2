package com.springbyexample.v2.skipandretry;

import java.util.Set;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.step.skip.LimitCheckingExceptionHierarchySkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import com.springbyexample.v2.skipandretry.exception.InvalidOrderException;
import com.springbyexample.v2.skipandretry.exception.TransientOrderProcessingException;

/**
 * Configures a fault-tolerant chunk-oriented Spring Batch job.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class SkipAndRetryJobConfiguration {

    private static final int CHUNK_SIZE = 3;

    private static final long SKIP_LIMIT = 2;

    private static final int MAX_RETRIES = 2;

    @Bean
    @StepScope
    public ItemReader<CustomerOrder> orderItemReader(SkipAndRetrySupport support,
            @Value("#{jobParameters['scenario']}") String scenario) {
        return new OrderItemReader(support.ordersFor(scenario));
    }

    @Bean
    public SkipPolicy invalidOrderSkipPolicy() {
        return new LimitCheckingExceptionHierarchySkipPolicy(Set.of(InvalidOrderException.class), SKIP_LIMIT);
    }

    @Bean
    public RetryPolicy transientOrderRetryPolicy() {
        return RetryPolicy.builder().maxRetries(MAX_RETRIES).includes(TransientOrderProcessingException.class).build();
    }

    @Bean
    public Step processOrdersStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            ItemReader<CustomerOrder> orderItemReader, OrderItemProcessor orderItemProcessor,
            OrderItemWriter orderItemWriter, SkipPolicy invalidOrderSkipPolicy, RetryPolicy transientOrderRetryPolicy) {

        return new ChunkOrientedStepBuilder<CustomerOrder, ProcessedOrder>("processOrdersStep", jobRepository,
                CHUNK_SIZE)
                .reader(orderItemReader)
                .processor(orderItemProcessor)
                .writer(orderItemWriter)
                .transactionManager(transactionManager)
                .faultTolerant()
                .skipPolicy(invalidOrderSkipPolicy)
                .retryPolicy(transientOrderRetryPolicy)
                .build();
    }

    @Bean
    public Job skipAndRetryJob(JobRepository jobRepository, Step processOrdersStep) {
        return new JobBuilder("skipAndRetryJob", jobRepository).start(processOrdersStep).build();
    }
}