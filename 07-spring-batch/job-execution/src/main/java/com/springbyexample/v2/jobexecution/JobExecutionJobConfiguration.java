package com.springbyexample.v2.jobexecution;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the job execution metadata example.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class JobExecutionJobConfiguration {

    @Bean
    public Step processMetadataStep(JobRepository jobRepository, JobExecutionMetadataTasklet tasklet) {
        return new StepBuilder("processMetadataStep", jobRepository).tasklet(tasklet).build();
    }

    @Bean
    public Job jobExecutionMetadataJob(JobRepository jobRepository, Step processMetadataStep) {
        return new JobBuilder("jobExecutionMetadataJob", jobRepository).start(processMetadataStep).build();
    }
}
