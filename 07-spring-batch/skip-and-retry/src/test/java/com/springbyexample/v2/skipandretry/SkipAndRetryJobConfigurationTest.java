package com.springbyexample.v2.skipandretry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for skip and retry behavior.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class SkipAndRetryJobConfigurationTest {

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private Job skipAndRetryJob;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private SkipAndRetrySupport support;

    @BeforeEach
    void setUp() {
        support.reset();
    }

    @Test
    void launchJob_shouldSkipInvalidOrdersAndRetryTransientFailures() throws Exception {
        JobExecution execution = launchJob("success");

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(1, execution.getStepExecutions().iterator().next().getProcessSkipCount());
        assertEquals(3, execution.getStepExecutions().iterator().next().getWriteCount());

        assertEquals(3, support.processingAttemptsFor(3L));
        assertEquals(3, support.writtenOrders().size());
        assertEquals(new ProcessedOrder(3L, "ORDER 3"), support.writtenOrders().get(1));
    }

    @Test
    void launchJob_shouldFailWhenRetryLimitIsExceeded() throws Exception {
        JobExecution execution = launchJob("retry-exhausted");

        assertEquals(BatchStatus.FAILED, execution.getStatus());
        assertEquals(3, support.processingAttemptsFor(1L));
        assertTrue(support.writtenOrders().isEmpty());
    }

    @Test
    void launchJob_shouldFailWhenSkipLimitIsExceeded() throws Exception {
        JobExecution execution = launchJob("skip-exhausted");

        assertEquals(BatchStatus.FAILED, execution.getStatus());

        StepExecution stepExecution = execution.getStepExecutions().iterator().next();

        assertEquals(2, stepExecution.getProcessSkipCount());
        assertTrue(support.writtenOrders().isEmpty());
    }

    @Test
    void launchJob_shouldCreateDistinctJobInstancesForDifferentParameters() throws Exception {
        JobExecution firstExecution = launchJob("success");
        JobExecution secondExecution = launchJob("success");

        assertEquals(BatchStatus.COMPLETED, firstExecution.getStatus());
        assertEquals(BatchStatus.COMPLETED, secondExecution.getStatus());

        assertNotEquals(firstExecution.getJobInstance().getInstanceId(), secondExecution.getJobInstance().getInstanceId());
    }

    @Test
    void launchJob_shouldPersistExecutionMetadata() throws Exception {
        JobExecution execution = launchJob("success");

        JobExecution storedExecution = jobRepository.getJobExecution(execution.getId());

        assertNotNull(storedExecution);
        assertEquals(BatchStatus.COMPLETED, storedExecution.getStatus());
        assertEquals(1, storedExecution.getStepExecutions().iterator().next().getProcessSkipCount());
    }

    private JobExecution launchJob(String scenario) throws Exception {
        JobParameters parameters = new JobParametersBuilder()
                .addJobParameter("scenario", scenario, String.class, true)
                .addJobParameter("run.id", UUID.randomUUID().toString(), String.class, true)
                .toJobParameters();

        return jobOperator.start(skipAndRetryJob, parameters);
    }
}