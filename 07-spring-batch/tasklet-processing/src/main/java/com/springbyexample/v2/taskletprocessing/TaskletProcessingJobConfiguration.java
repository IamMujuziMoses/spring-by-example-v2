package com.springbyexample.v2.taskletprocessing;

import java.util.List;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Tasklet Processing job.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class TaskletProcessingJobConfiguration {

    private static final List<String> EXPIRED_ORDER_IDS = List.of("order-101", "order-102", "order-103", "order-104");

    @Bean
    public Tasklet orderCleanupTasklet() {
        return new OrderCleanupTasklet(EXPIRED_ORDER_IDS);
    }

    @Bean
    public Step cleanupOrdersStep(JobRepository jobRepository, Tasklet orderCleanupTasklet) {

        return new StepBuilder("cleanupOrdersStep", jobRepository).tasklet(orderCleanupTasklet).build();
    }

    @Bean
    public Job taskletProcessingJob(JobRepository jobRepository, Step cleanupOrdersStep) {

        return new JobBuilder("taskletProcessingJob", jobRepository).start(cleanupOrdersStep).build();
    }
}
