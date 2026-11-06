package com.springbyexample.v2.restartability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

/**
 * Unit tests for {@link FirstStepTasklet}.
 *
 * @author Mujuzi Moses
 */
public class FirstStepTaskletTest {

    private FirstStepTasklet tasklet;

    private StepContribution contribution;

    private ChunkContext chunkContext;

    @BeforeEach
    void setUp() {
        tasklet = new FirstStepTasklet();

        JobInstance jobInstance = new JobInstance(1L, "restartabilityJob");
        JobExecution jobExecution = new JobExecution(1L, jobInstance, new JobParameters());

        StepExecution stepExecution = new StepExecution(1L, "firstStep", jobExecution);

        contribution = new StepContribution(stepExecution);
        chunkContext = new ChunkContext(new StepContext(stepExecution));
    }

    @Test
    void execute_shouldReturnFinished() {
        RepeatStatus result = tasklet.execute(contribution, chunkContext);

        assertThat(result).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    void execute_shouldIncrementExecutionCount() {
        tasklet.execute(contribution, chunkContext);

        assertThat(tasklet.getExecutionCount()).isEqualTo(1);
    }

    @Test
    void execute_shouldIncrementExecutionCountForEachInvocation() {
        tasklet.execute(contribution, chunkContext);
        tasklet.execute(contribution, chunkContext);

        assertThat(tasklet.getExecutionCount()).isEqualTo(2);
    }
}
