package com.springbyexample.v2.itemprocessors;

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
 * Tests the Spring Batch item processor job configuration.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(properties = "spring.batch.job.enabled=false")
public class ItemProcessorJobConfigurationTest {

    @Autowired
    private Job processCustomersJob;

    @Autowired
    private JobOperator jobOperator;

    /**
     * Verifies that the job reads, processes, filters, and writes customer
     * names successfully.
     *
     * @throws Exception if job execution fails
     */
    @Test
    void processCustomersJob_shouldProcessAndFilterCustomerNames() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addJobParameter("run.id", UUID.randomUUID().toString(), String.class, true)
                .toJobParameters();

        JobExecution jobExecution = jobOperator.start(processCustomersJob, jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getStepExecutions()).hasSize(1);

        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();

        assertThat(stepExecution.getStepName()).isEqualTo("processCustomersStep");
        assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(stepExecution.getReadCount()).isEqualTo(5);
        assertThat(stepExecution.getProcessSkipCount()).isZero();
        assertThat(stepExecution.getFilterCount()).isEqualTo(1);
        assertThat(stepExecution.getWriteCount()).isEqualTo(4);
        assertThat(stepExecution.getCommitCount()).isEqualTo(3);
    }

}
