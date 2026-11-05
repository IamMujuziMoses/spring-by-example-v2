package com.springbyexample.v2.jobexecution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

/**
 * Tests for {@link JobExecutionMetadataTasklet}.
 *
 * @author Mujuzi Moses
 */
public class JobExecutionMetadataTaskletTest {

    @Test
    void execute_shouldRecordProcessedRecordCount() throws Exception {
        JobExecutionMetadataTasklet tasklet = new JobExecutionMetadataTasklet();

        ExecutionContext executionContext = executeTasklet(tasklet);

        assertThat(executionContext.getInt("metadata.processed.records")).isEqualTo(5);
    }

    @Test
    void execute_shouldRecordCompletedProcessingStatus() throws Exception {
        JobExecutionMetadataTasklet tasklet = new JobExecutionMetadataTasklet();

        ExecutionContext executionContext = executeTasklet(tasklet);

        assertThat(executionContext.getString("metadata.processing.status")).isEqualTo("COMPLETED");
    }

    @Test
    void execute_shouldReturnFinished() throws Exception {
        JobExecutionMetadataTasklet tasklet = new JobExecutionMetadataTasklet();

        StepContribution contribution = mock(StepContribution.class);
        StepExecution stepExecution = mock(StepExecution.class);

        when(contribution.getStepExecution()).thenReturn(stepExecution);
        when(stepExecution.getExecutionContext()).thenReturn(new ExecutionContext());

        assertThat(tasklet.execute(contribution, null)).isEqualTo(RepeatStatus.FINISHED);
    }

    private ExecutionContext executeTasklet(JobExecutionMetadataTasklet tasklet) throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        StepContribution contribution = mock(StepContribution.class);
        StepExecution stepExecution = mock(StepExecution.class);

        when(contribution.getStepExecution()).thenReturn(stepExecution);
        when(stepExecution.getExecutionContext()).thenReturn(executionContext);

        tasklet.execute(contribution, null);

        return executionContext;
    }
}
