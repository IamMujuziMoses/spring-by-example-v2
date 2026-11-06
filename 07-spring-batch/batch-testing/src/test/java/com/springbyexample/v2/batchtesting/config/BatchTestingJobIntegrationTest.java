package com.springbyexample.v2.batchtesting.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.batchtesting.BatchTestingApplication;
import com.springbyexample.v2.batchtesting.support.BatchTestingSupport;

/**
 * Integration tests for the Spring Batch Testing example job.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(classes = BatchTestingApplication.class)
@SpringBatchTest
public class BatchTestingJobIntegrationTest {

    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;

    @Autowired
    private Job batchTestingJob;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private BatchTestingSupport support;

    @BeforeEach
    void setUp() {
        jobOperatorTestUtils.setJob(batchTestingJob);
        support.reset();
    }

    @Test
    void startJob_shouldCompleteAndWriteAllOrders() throws Exception {
        JobExecution execution = jobOperatorTestUtils.startJob();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getStepExecutions()).hasSize(2);
        assertThat(support.writtenOrders()).hasSize(5);
        assertThat(support.summaryProcessedCount()).isEqualTo(5);
    }

    @Test
    void startStep_shouldExecuteOnlyRequestedStep() {
        JobExecution execution = jobOperatorTestUtils.startStep("processOrdersStep");

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getStepExecutions()).hasSize(1);
        assertThat(execution.getStepExecutions().iterator().next().getStepName()).isEqualTo("processOrdersStep");
        assertThat(support.writtenOrders()).hasSize(5);
        assertThat(support.summaryProcessedCount()).isEqualTo(-1);
    }

    @Test
    void startJob_shouldExposeStepExecutionCounts() throws Exception {
        JobExecution execution = jobOperatorTestUtils.startJob();

        StepExecution processingStep = execution.getStepExecutions()
                .stream()
                .filter(step -> step.getStepName().equals("processOrdersStep"))
                .findFirst()
                .orElseThrow();

        assertThat(processingStep.getReadCount()).isEqualTo(5);
        assertThat(processingStep.getWriteCount()).isEqualTo(5);
        assertThat(processingStep.getCommitCount()).isEqualTo(3);
    }

    @Test
    void startJob_shouldPersistExecutionMetadata() throws Exception {
        JobExecution execution = jobOperatorTestUtils.startJob();

        JobExecution persistedExecution = jobRepository.getJobExecution(execution.getId());

        assertThat(persistedExecution).isNotNull();
        assertThat(persistedExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(persistedExecution.getJobInstance().getJobName()).isEqualTo("batchTestingJob");
    }

    @Test
    void startJobWithCustomParameters_shouldUseSuppliedParameters() throws Exception {
        JobParameters parameters = new JobParametersBuilder().addString("test.run.id", UUID.randomUUID().toString())
                .toJobParameters();

        JobExecution execution = jobOperatorTestUtils.startJob(parameters);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getJobParameters().getString("test.run.id"))
                .isEqualTo(parameters.getString("test.run.id"));
    }
}
