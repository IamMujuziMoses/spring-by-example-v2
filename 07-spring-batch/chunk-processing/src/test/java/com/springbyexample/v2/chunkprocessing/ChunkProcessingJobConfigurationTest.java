package com.springbyexample.v2.chunkprocessing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
 * Integration tests for the chunk processing batch job.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(properties = "spring.batch.job.enabled=false")
public class ChunkProcessingJobConfigurationTest {

    @Autowired
    private Job chunkProcessingJob;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private ChunkRecordingItemWriter chunkRecordingItemWriter;

    @Test
    void chunkProcessingJob_shouldProcessAndWriteItemsInChunks() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addJobParameter("run.id", UUID.randomUUID().toString(), String.class, true)
                .toJobParameters();

        JobExecution jobExecution = jobOperator.start(chunkProcessingJob, jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getStepExecutions()).hasSize(1);

        var stepExecution = jobExecution.getStepExecutions().iterator().next();

        assertThat(stepExecution.getStepName()).isEqualTo("processOrdersStep");
        assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        assertThat(stepExecution.getReadCount()).isEqualTo(7);
        assertThat(stepExecution.getWriteCount()).isEqualTo(7);
        assertThat(stepExecution.getFilterCount()).isZero();
        assertThat(stepExecution.getCommitCount()).isEqualTo(3);

        assertThat(chunkRecordingItemWriter.getWrittenChunks()).containsExactly(
                List.of("ORDER-101", "ORDER-102", "ORDER-103"),
                List.of("ORDER-104", "ORDER-105", "ORDER-106"),
                List.of("ORDER-107")
        );
    }
}