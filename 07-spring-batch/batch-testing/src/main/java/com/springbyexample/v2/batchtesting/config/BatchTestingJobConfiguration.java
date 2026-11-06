package com.springbyexample.v2.batchtesting.config;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.springbyexample.v2.batchtesting.domain.Order;
import com.springbyexample.v2.batchtesting.domain.ProcessedOrder;
import com.springbyexample.v2.batchtesting.processor.OrderItemProcessor;
import com.springbyexample.v2.batchtesting.reader.OrderItemReader;
import com.springbyexample.v2.batchtesting.support.BatchTestingSupport;
import com.springbyexample.v2.batchtesting.writer.OrderItemWriter;

/**
 * Configures the Spring Batch Testing example job and its steps.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class BatchTestingJobConfiguration {

    @Bean
    @StepScope
    public OrderItemReader orderItemReader(BatchTestingSupport support) {
        return new OrderItemReader(support.orders());
    }

    @Bean
    public Step processOrdersStep(JobRepository jobRepository, OrderItemReader orderItemReader,
            OrderItemProcessor orderItemProcessor, OrderItemWriter orderItemWriter) {

        return new org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder
                <Order, ProcessedOrder>("processOrdersStep", jobRepository, 2)
                .reader(orderItemReader).processor(orderItemProcessor).writer(orderItemWriter).build();
    }

    @Bean
    public Step summarizeOrdersStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            BatchTestingSupport support) {

        Tasklet summarizeOrdersTasklet = (contribution, chunkContext) -> {
            int processedCount = support.writtenOrders().size();

            contribution.getStepExecution().getJobExecution().getExecutionContext()
                    .putInt("batch.summary.processed.count", processedCount);

            support.recordSummary(processedCount);

            return RepeatStatus.FINISHED;
        };

        return new StepBuilder("summarizeOrdersStep", jobRepository)
                .tasklet(summarizeOrdersTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job batchTestingJob(JobRepository jobRepository, Step processOrdersStep, Step summarizeOrdersStep) {
        return new JobBuilder("batchTestingJob", jobRepository).start(processOrdersStep).next(summarizeOrdersStep)
                .build();
    }
}
