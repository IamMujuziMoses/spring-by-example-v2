package com.springbyexample.v2.restartability;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configures the restartability demonstration job and its steps.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class RestartabilityJobConfiguration {

    @Bean
    public Step firstStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            FirstStepTasklet tasklet) {

        return new StepBuilder("firstStep", jobRepository).tasklet(tasklet, transactionManager).build();
    }

    @Bean
    public Step restartableStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            RestartableTasklet tasklet) {

        return new StepBuilder("restartableStep", jobRepository).tasklet(tasklet, transactionManager).build();
    }

    @Bean
    public Job restartabilityJob(JobRepository jobRepository, Step firstStep, Step restartableStep) {

        return new JobBuilder("restartabilityJob", jobRepository).start(firstStep).next(restartableStep).build();
    }
}
