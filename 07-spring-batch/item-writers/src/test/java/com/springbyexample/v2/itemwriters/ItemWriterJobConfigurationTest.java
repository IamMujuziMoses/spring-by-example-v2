package com.springbyexample.v2.itemwriters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for the item writer batch job.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(properties = "spring.batch.job.enabled=false")
public class ItemWriterJobConfigurationTest {

    @Autowired
    private Job writeCustomersJob;

    @Autowired
    private JobOperator jobOperator;

    @Test
    void writeCustomersJob_shouldWriteProcessedCustomersSuccessfully() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addJobParameter("run.id", UUID.randomUUID().toString(), String.class, true)
                .toJobParameters();

        JobExecution jobExecution = jobOperator.start(writeCustomersJob, jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getStepExecutions()).hasSize(1);

        var stepExecution = jobExecution.getStepExecutions().iterator().next();

        assertThat(stepExecution.getStepName()).isEqualTo("writeCustomersStep");
        assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(stepExecution.getReadCount()).isEqualTo(5);
        assertThat(stepExecution.getWriteCount()).isEqualTo(4);
        assertThat(stepExecution.getFilterCount()).isEqualTo(1);
        assertThat(stepExecution.getProcessSkipCount()).isZero();
        assertThat(stepExecution.getCommitCount()).isEqualTo(3);
    }
}
