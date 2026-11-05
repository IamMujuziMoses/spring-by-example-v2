package com.springbyexample.v2.taskletprocessing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for the Tasklet Processing job.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(properties = "spring.batch.job.enabled=false")
public class TaskletProcessingJobConfigurationTest {

    @Autowired
    private Job taskletProcessingJob;

    @Autowired
    private JobOperator jobOperator;

    @Test
    void taskletProcessingJob_shouldCompleteSuccessfully() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addJobParameter("run.id", UUID.randomUUID().toString(), String.class, true)
                .toJobParameters();

        JobExecution jobExecution = jobOperator.start(taskletProcessingJob, jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getStepExecutions()).hasSize(1);

        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();

        assertThat(stepExecution.getStepName()).isEqualTo("cleanupOrdersStep");
        assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(stepExecution.getExecutionContext().getInt("orders.archived.count")).isEqualTo(4);
        assertThat(stepExecution.getExecutionContext().getString("cleanup.status")).isEqualTo("COMPLETED");
    }
}
