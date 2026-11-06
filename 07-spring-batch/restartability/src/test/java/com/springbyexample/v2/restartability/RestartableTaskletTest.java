package com.springbyexample.v2.restartability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
 * Unit tests for {@link RestartableTasklet}.
 *
 * @author Mujuzi Moses
 */
public class RestartableTaskletTest {

    private RestartableTasklet tasklet;

    private StepContribution contribution;

    private ChunkContext chunkContext;

    private JobExecution jobExecution;

    @BeforeEach
    void setUp() {
        tasklet = new RestartableTasklet();

        JobInstance jobInstance = new JobInstance(1L, "restartabilityJob");
        jobExecution = new JobExecution(1L, jobInstance, new JobParameters());

        StepExecution stepExecution = new StepExecution(1L, "restartableStep", jobExecution);

        contribution = new StepContribution(stepExecution);
        chunkContext = new ChunkContext(new StepContext(stepExecution));
    }

    @Test
    void execute_shouldFailOnFirstAttempt() {
        assertThatThrownBy(() -> tasklet.execute(contribution, chunkContext)).isInstanceOf(IllegalStateException.class)
                .hasMessage("Simulated failure for restartability demonstration.");

        assertThat(jobExecution.getExecutionContext().getInt(RestartableTasklet.ATTEMPT_COUNT)).isEqualTo(1);
        assertThat(tasklet.getExecutionCount()).isEqualTo(1);
    }

    @Test
    void execute_shouldSucceedOnSecondAttempt() {
        jobExecution.getExecutionContext().putInt(RestartableTasklet.ATTEMPT_COUNT, 1);

        RepeatStatus result = tasklet.execute(contribution, chunkContext);

        assertThat(result).isEqualTo(RepeatStatus.FINISHED);
        assertThat(jobExecution.getExecutionContext().getInt(RestartableTasklet.ATTEMPT_COUNT)).isEqualTo(2);
    }

    @Test
    void execute_shouldIncrementExistingAttemptCount() {
        jobExecution.getExecutionContext().putInt(RestartableTasklet.ATTEMPT_COUNT, 2);

        RepeatStatus result = tasklet.execute(contribution, chunkContext);

        assertThat(result).isEqualTo(RepeatStatus.FINISHED);
        assertThat(jobExecution.getExecutionContext().getInt(RestartableTasklet.ATTEMPT_COUNT)).isEqualTo(3);
    }
}
