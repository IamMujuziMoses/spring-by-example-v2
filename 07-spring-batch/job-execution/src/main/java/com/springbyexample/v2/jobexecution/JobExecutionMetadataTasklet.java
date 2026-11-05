package com.springbyexample.v2.jobexecution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * Tasklet that performs a simple operation and records execution metadata.
 *
 * @author Mujuzi Moses
 */
@Component
public class JobExecutionMetadataTasklet implements Tasklet {

    private static final Log LOG = LogFactory.getLog(JobExecutionMetadataTasklet.class);

    private static final int PROCESSED_RECORDS = 5;

    @Override
    public RepeatStatus execute(StepContribution contribution, @NonNull ChunkContext chunkContext) {
        LOG.info("Processing batch records");

        ExecutionContext executionContext = contribution.getStepExecution().getExecutionContext();
        executionContext.putInt("metadata.processed.records", PROCESSED_RECORDS);
        executionContext.putString("metadata.processing.status", "COMPLETED");

        LOG.info("Processed " + PROCESSED_RECORDS + " records");

        return RepeatStatus.FINISHED;
    }
}
