package com.springbyexample.v2.jobsandsteps;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests the Spring Batch job and its step execution.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(properties = "spring.batch.job.enabled=false")
public class BatchJobConfigurationTest {

    @Autowired
    private Job reportGenerationJob;

    @Autowired
    private JobOperator jobOperator;

    /**
     * Verifies that the report generation job executes both steps
     * successfully.
     *
     * @throws Exception if job execution fails
     */
    @Test
    void shouldExecuteJobWithBothSteps() throws Exception {
        var jobParameters = new JobParametersBuilder().addString("run.id", UUID.randomUUID().toString())
                .toJobParameters();

        JobExecution jobExecution = jobOperator.start(reportGenerationJob, jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        var stepNames = jobExecution.getStepExecutions().stream().map(StepExecution::getStepName).toList();

        assertThat(stepNames).containsExactly("prepareReportStep", "generateReportStep");

        assertThat(jobExecution.getStepExecutions()).allSatisfy(stepExecution ->
                        assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED));
    }

}
