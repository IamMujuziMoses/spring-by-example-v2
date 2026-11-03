package com.springbyexample.v2.jobparameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests the execution of a Spring Batch job using runtime job parameters.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(properties = "spring.batch.job.enabled=false")
public class JobParametersConfigurationTest {

    @Autowired
    private Job reportGenerationJob;

    @Autowired
    private JobOperator jobOperator;

    /**
     * Verifies that the job executes successfully using supplied parameters
     * and makes those parameters available to the step.
     *
     * @throws Exception if job execution fails
     */
    @Test
    void shouldExecuteJobUsingSuppliedParameters() throws Exception {
        var jobParameters = JobParametersConfiguration.createJobParameters();

        JobExecution jobExecution = jobOperator.start(reportGenerationJob, jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getStepExecutions()).hasSize(1);

        var stepExecution = jobExecution.getStepExecutions().iterator().next();

        assertThat(stepExecution.getStepName()).isEqualTo("generateReportStep");
        assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        var executionContext = stepExecution.getExecutionContext();

        assertThat(executionContext.getString("report.date")).isEqualTo("2026-11-03");
        assertThat(executionContext.getString("report.format")).isEqualTo("CSV");
        assertThat(executionContext.getString("requested.by")).isEqualTo("batch-user");
    }

}
