package com.springbyexample.v2.jobparameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures a Spring Batch job that receives and uses runtime parameters.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class JobParametersConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobParametersConfiguration.class);

    /**
     * Creates a step-scoped tasklet that reads report details from job
     * parameters when the step executes.
     *
     * @param reportDate   date associated with the report
     * @param reportFormat requested report format
     * @param requestedBy  person or process requesting the report
     * @return the report generation tasklet
     */
    @Bean
    @StepScope
    public Tasklet generateReportTasklet(@Value("#{jobParameters['report.date']}") String reportDate,
            @Value("#{jobParameters['report.format']}") String reportFormat,
            @Value("#{jobParameters['requested.by']}") String requestedBy) {

        return (contribution, chunkContext) -> {
            LOGGER.info("Generating report for date: {}", reportDate);
            LOGGER.info("Report format: {}", reportFormat);
            LOGGER.info("Requested by: {}", requestedBy);

            var executionContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();

            executionContext.putString("report.date", reportDate);
            executionContext.putString("report.format", reportFormat);
            executionContext.putString("requested.by", requestedBy);

            return RepeatStatus.FINISHED;
        };
    }

    /**
     * Creates the step responsible for generating the report.
     *
     * @param jobRepository         repository used to manage batch metadata
     * @param generateReportTasklet tasklet that generates the report
     * @return the report generation step
     */
    @Bean
    public Step generateReportStep(JobRepository jobRepository, Tasklet generateReportTasklet) {

        return new StepBuilder("generateReportStep", jobRepository).tasklet(generateReportTasklet).build();
    }

    /**
     * Creates the report generation job.
     *
     * @param jobRepository      repository used to manage batch metadata
     * @param generateReportStep step that generates the report
     * @return the report generation job
     */
    @Bean
    public Job reportGenerationJob(JobRepository jobRepository, Step generateReportStep) {

        return new JobBuilder("reportGenerationJob", jobRepository).start(generateReportStep).build();
    }

    /**
     * Creates sample job parameters for launching the report generation job.
     *
     * <p>The report date and format identify the job instance. The requester
     * is non-identifying and does not affect job instance identity.</p>
     *
     * @return sample job parameters
     */
    public static JobParameters createJobParameters() {
        return new JobParametersBuilder()
                .addJobParameter("report.date", "2026-11-03", String.class, true)
                .addJobParameter("report.format", "CSV", String.class, true)
                .addJobParameter("requested.by", "batch-user", String.class, false)
                .toJobParameters();
    }

}
