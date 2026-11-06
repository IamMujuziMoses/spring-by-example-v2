package com.springbyexample.v2.restartability;

import java.util.concurrent.atomic.AtomicInteger;

import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * Tasklet that deliberately fails on its first execution and succeeds on restart.
 * <p>
 * The attempt count is stored in the job execution context so that it can
 * be restored when Spring Batch restarts the job.
 *
 * @author Mujuzi Moses
 */
@Component
public class RestartableTasklet implements Tasklet {

    static final String ATTEMPT_COUNT = "restartability.attempt.count";

    private final AtomicInteger executionCount = new AtomicInteger();

    @Override
    public RepeatStatus execute(StepContribution contribution, @NonNull ChunkContext chunkContext) {

        var jobExecutionContext = contribution.getStepExecution().getJobExecution().getExecutionContext();

        int attempt = jobExecutionContext.getInt(ATTEMPT_COUNT, 0) + 1;

        jobExecutionContext.putInt(ATTEMPT_COUNT, attempt);
        executionCount.incrementAndGet();

        if (attempt == 1) {
            throw new IllegalStateException("Simulated failure for restartability demonstration.");
        }

        return RepeatStatus.FINISHED;
    }

    public int getExecutionCount() {
        return executionCount.get();
    }

    public void reset() {
        executionCount.set(0);
    }
}
