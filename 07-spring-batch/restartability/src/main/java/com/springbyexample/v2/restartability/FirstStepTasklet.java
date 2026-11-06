package com.springbyexample.v2.restartability;

import java.util.concurrent.atomic.AtomicInteger;

import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * Tasklet representing the first successfully completed step.
 *
 * @author Mujuzi Moses
 */
@Component
public class FirstStepTasklet implements Tasklet {

    private final AtomicInteger executionCount = new AtomicInteger();

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {

        executionCount.incrementAndGet();

        return RepeatStatus.FINISHED;
    }

    public int getExecutionCount() {
        return executionCount.get();
    }

    public void reset() {
        executionCount.set(0);
    }
}
