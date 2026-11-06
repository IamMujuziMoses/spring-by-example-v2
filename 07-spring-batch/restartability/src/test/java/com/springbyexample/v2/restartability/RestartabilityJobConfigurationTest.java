package com.springbyexample.v2.restartability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for Spring Batch job restartability.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class RestartabilityJobConfigurationTest {

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private Job restartabilityJob;

    @Autowired
    private FirstStepTasklet firstStepTasklet;

    @Autowired
    private RestartableTasklet restartableTasklet;

    @BeforeEach
    void setUp() {
        firstStepTasklet.reset();
        restartableTasklet.reset();
    }

    @Test
    void restartJob_shouldNotReexecuteCompletedSteps() throws Exception {
        JobExecution firstExecution = jobOperator.start(restartabilityJob, createJobParameters());

        assertThat(firstExecution.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(firstStepTasklet.getExecutionCount()).isEqualTo(1);

        JobExecution restartedExecution = jobOperator.restart(firstExecution);

        assertThat(restartedExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // The previously completed step must not execute again.
        assertThat(firstStepTasklet.getExecutionCount()).isEqualTo(1);

        // The failed step must execute again.
        assertThat(restartableTasklet.getExecutionCount()).isEqualTo(2);
    }

    @Test
    void restartJob_shouldRestorePersistedExecutionContext() throws  Exception {
        JobExecution firstExecution = jobOperator.start(restartabilityJob, createJobParameters());

        assertThat(firstExecution.getStatus()).isEqualTo(BatchStatus.FAILED);

        // Reload the failed execution from the JDBC-backed repository.
        JobExecution persistedExecution = jobRepository.getJobExecution(firstExecution.getId());

        assertThat(persistedExecution).isNotNull();
        assertThat(persistedExecution.getExecutionContext().getInt(RestartableTasklet.ATTEMPT_COUNT)).isEqualTo(1);

        // Restart using the persisted execution.
        JobExecution restartedExecution = jobOperator.restart(persistedExecution);

        assertThat(restartedExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // Reload the restarted execution and verify its persisted state.
        JobExecution persistedRestartedExecution = jobRepository.getJobExecution(restartedExecution.getId());

        assertThat(persistedRestartedExecution).isNotNull();
        assertThat(persistedRestartedExecution.getExecutionContext().getInt(RestartableTasklet.ATTEMPT_COUNT)).isEqualTo(2);
    }

    @Test
    void restartJob_shouldCompleteSuccessfullyAfterRestart() throws  Exception {
        JobExecution firstExecution = jobOperator.start(restartabilityJob, createJobParameters());

        assertThat(firstExecution.getStatus()).isEqualTo(BatchStatus.FAILED);

        JobExecution restartedExecution = jobOperator.restart(firstExecution);

        assertThat(restartedExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // The first step completed in the original execution.
        assertThat(findStepExecution(firstExecution, "firstStep").getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // The failed step completed in the restarted execution.
        assertThat(findStepExecution(restartedExecution, "restartableStep").getStatus())
                .isEqualTo(BatchStatus.COMPLETED);
        assertThat(restartedExecution.getId()).isNotEqualTo(firstExecution.getId());
        assertThat(restartedExecution.getJobInstance().getInstanceId())
                .isEqualTo(firstExecution.getJobInstance().getInstanceId());
    }

    @Test
    void restartJob_shouldNotRestartCompletedExecution() throws  Exception {
        JobExecution firstExecution = jobOperator.start(restartabilityJob, createJobParameters());

        assertThat(firstExecution.getStatus()).isEqualTo(BatchStatus.FAILED);

        JobExecution completedExecution = jobOperator.restart(firstExecution);

        assertThat(completedExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThatThrownBy(() -> jobOperator.restart(completedExecution)).isInstanceOf(JobRestartException.class);
    }

    @Test
    void startJob_withDifferentParameters_shouldCreateNewJobInstance() throws Exception {
        JobExecution firstExecution = jobOperator.start(restartabilityJob, createJobParameters());
        JobExecution secondExecution = jobOperator.start(restartabilityJob, createJobParameters());

        assertThat(firstExecution.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(secondExecution.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(secondExecution.getJobInstance().getInstanceId())
                .isNotEqualTo(firstExecution.getJobInstance().getInstanceId());
        assertThat(firstStepTasklet.getExecutionCount()).isEqualTo(2);
        assertThat(restartableTasklet.getExecutionCount()).isEqualTo(2);
    }

    private StepExecution findStepExecution(JobExecution jobExecution, String stepName) {

        return jobExecution.getStepExecutions().stream()
                .filter(stepExecution -> stepExecution.getStepName().equals(stepName)).findFirst()
                .orElseThrow(() -> new AssertionError("Step execution not found: " + stepName));
    }

    private JobParameters createJobParameters() {
        return new JobParametersBuilder().addString("run.id", UUID.randomUUID().toString()).toJobParameters();
    }
}
