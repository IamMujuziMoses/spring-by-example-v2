package com.springbyexample.v2.jobsandsteps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Spring Batch job and its steps.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class BatchJobConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchJobConfiguration.class);

    /**
     * Creates the first step, which prepares the report.
     *
     * @param jobRepository repository used to manage batch metadata
     * @return the report preparation step
     */
    @Bean
    public Step prepareReportStep(JobRepository jobRepository) {
        return new StepBuilder("prepareReportStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    LOGGER.info("Preparing the report...");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    /**
     * Creates the second step, which generates the report.
     *
     * @param jobRepository repository used to manage batch metadata
     * @return the report generation step
     */
    @Bean
    public Step generateReportStep(JobRepository jobRepository) {
        return new StepBuilder("generateReportStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    LOGGER.info("Generating the report...");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    /**
     * Creates a job that executes the report preparation and generation
     * steps sequentially.
     *
     * @param jobRepository      repository used to manage batch metadata
     * @param prepareReportStep  step that prepares the report
     * @param generateReportStep step that generates the report
     * @return the report generation job
     */
    @Bean
    public Job reportGenerationJob(JobRepository jobRepository, Step prepareReportStep, Step generateReportStep) {
        return new JobBuilder("reportGenerationJob", jobRepository).start(prepareReportStep)
                .next(generateReportStep).build();
    }

}