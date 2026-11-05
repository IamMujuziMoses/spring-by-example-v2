package com.springbyexample.v2.taskletprocessing;

import java.util.List;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

/**
 * Tasklet that demonstrates a single order cleanup operation.
 *
 * @author Mujuzi Moses
 */
public class OrderCleanupTasklet implements Tasklet {

    private static final Log LOG = LogFactory.getLog(OrderCleanupTasklet.class);

    private static final String ARCHIVED_COUNT_KEY = "orders.archived.count";

    private static final String CLEANUP_STATUS_KEY = "cleanup.status";

    private final List<String> expiredOrderIds;

    public OrderCleanupTasklet(List<String> expiredOrderIds) {
        this.expiredOrderIds = List.copyOf(
                Objects.requireNonNull(expiredOrderIds, "expiredOrderIds must not be null"));
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, @NonNull ChunkContext chunkContext) {

        LOG.info("Starting expired order cleanup");

        expiredOrderIds.forEach(orderId -> LOG.info("Archiving expired order: " + orderId));

        ExecutionContext executionContext = contribution.getStepExecution().getExecutionContext();

        executionContext.putInt(ARCHIVED_COUNT_KEY, expiredOrderIds.size());
        executionContext.putString(CLEANUP_STATUS_KEY, "COMPLETED");

        LOG.info("Archived " + expiredOrderIds.size() + " expired orders");

        return RepeatStatus.FINISHED;
    }
}
