package com.springbyexample.v2.taskletprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

/**
 * Tests for {@link OrderCleanupTasklet}.
 *
 * @author Mujuzi Moses
 */
public class OrderCleanupTaskletTest {

    @Test
    void execute_shouldRecordArchivedOrderCount() throws Exception {
        OrderCleanupTasklet tasklet = new OrderCleanupTasklet(List.of("order-101", "order-102", "order-103"));

        ExecutionContext executionContext = executeTasklet(tasklet);

        assertThat(executionContext.getInt("orders.archived.count")).isEqualTo(3);
    }

    @Test
    void execute_shouldRecordCompletedCleanupStatus() throws Exception {
        OrderCleanupTasklet tasklet = new OrderCleanupTasklet(List.of("order-101", "order-102"));

        ExecutionContext executionContext = executeTasklet(tasklet);

        assertThat(executionContext.getString("cleanup.status")).isEqualTo("COMPLETED");
    }

    @Test
    void execute_shouldReturnFinished() throws Exception {
        OrderCleanupTasklet tasklet = new OrderCleanupTasklet(List.of("order-101"));

        StepContribution contribution = mock(StepContribution.class);
        StepExecution stepExecution = mock(StepExecution.class);

        when(contribution.getStepExecution()).thenReturn(stepExecution);
        when(stepExecution.getExecutionContext()).thenReturn(new ExecutionContext());

        assertThat(tasklet.execute(contribution, null)).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    void execute_shouldHandleEmptyOrderList() throws Exception {
        OrderCleanupTasklet tasklet = new OrderCleanupTasklet(List.of());

        ExecutionContext executionContext = executeTasklet(tasklet);

        assertThat(executionContext.getInt("orders.archived.count")).isZero();
        assertThat(executionContext.getString("cleanup.status")).isEqualTo("COMPLETED");
    }

    @Test
    void execute_shouldHandleSingleOrder() throws Exception {
        OrderCleanupTasklet tasklet = new OrderCleanupTasklet(List.of("order-101"));

        ExecutionContext executionContext = executeTasklet(tasklet);

        assertThat(executionContext.getInt("orders.archived.count")).isEqualTo(1);
    }

    @Test
    void execute_shouldHandleMultipleOrders() throws Exception {
        OrderCleanupTasklet tasklet = new OrderCleanupTasklet(List.of("order-101", "order-102", "order-103", "order-104"));

        ExecutionContext executionContext = executeTasklet(tasklet);

        assertThat(executionContext.getInt("orders.archived.count")).isEqualTo(4);
    }

    @Test
    void constructor_shouldRejectNullOrderIds() {
        assertThatNullPointerException().isThrownBy(() -> new OrderCleanupTasklet(null));
    }

    private ExecutionContext executeTasklet(OrderCleanupTasklet tasklet) throws Exception {

        ExecutionContext executionContext = new ExecutionContext();

        StepContribution contribution = mock(StepContribution.class);
        StepExecution stepExecution = mock(StepExecution.class);

        when(contribution.getStepExecution()).thenReturn(stepExecution);
        when(stepExecution.getExecutionContext()).thenReturn(executionContext);

        tasklet.execute(contribution, null);

        return executionContext;
    }
}
