package com.springbyexample.v2.jobexecution;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST endpoints for launching jobs and inspecting execution metadata.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/jobs/executions")
public class JobExecutionController {

    private final Job jobExecutionMetadataJob;

    private final JobOperator jobOperator;

    private final JobRepository jobRepository;

    public JobExecutionController(Job jobExecutionMetadataJob, JobOperator jobOperator, JobRepository jobRepository) {
        this.jobExecutionMetadataJob = jobExecutionMetadataJob;
        this.jobOperator = jobOperator;
        this.jobRepository = jobRepository;

        System.out.println("JobRepository implementation: " + jobRepository.getClass().getName());
    }

    @PostMapping
    public Map<String, Object> launchJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addJobParameter("run.id", UUID.randomUUID().toString(), String.class, true)
                .toJobParameters();

        JobExecution jobExecution = jobOperator.start(jobExecutionMetadataJob, jobParameters);

        return toMetadata(jobExecution);
    }

    @GetMapping("/{executionId}")
    public Map<String, Object> getExecution(@PathVariable long executionId) {

        JobExecution jobExecution;

        try {
            jobExecution = jobRepository.getJobExecution(executionId);
        } catch (EmptyResultDataAccessException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job execution not found: " + executionId);
        }

        if (jobExecution == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job execution not found: " + executionId);
        }

        return toMetadata(jobExecution);
    }

    private Map<String, Object> toMetadata(JobExecution jobExecution) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put("executionId", jobExecution.getId());
        metadata.put("jobName", jobExecution.getJobInstance().getJobName());
        metadata.put("jobInstanceId", jobExecution.getJobInstance().getInstanceId());
        metadata.put("status", jobExecution.getStatus().toString());
        metadata.put("startTime", jobExecution.getStartTime());
        metadata.put("endTime", jobExecution.getEndTime());
        metadata.put("exitStatus", jobExecution.getExitStatus().getExitCode());
        metadata.put("jobParameters", jobExecution.getJobParameters());
        metadata.put("steps", jobExecution.getStepExecutions().stream().map(this::toStepMetadata).toList());

        return metadata;
    }

    private Map<String, Object> toStepMetadata(StepExecution stepExecution) {
        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put("stepExecutionId", stepExecution.getId());
        metadata.put("stepName", stepExecution.getStepName());
        metadata.put("status", stepExecution.getStatus().toString());
        metadata.put("startTime", stepExecution.getStartTime());
        metadata.put("endTime", stepExecution.getEndTime());
        metadata.put("readCount", stepExecution.getReadCount());
        metadata.put("writeCount", stepExecution.getWriteCount());
        metadata.put("commitCount", stepExecution.getCommitCount());
        metadata.put("rollbackCount", stepExecution.getRollbackCount());
        metadata.put("processedRecords",
                stepExecution.getExecutionContext().getInt("metadata.processed.records", 0));

        return metadata;
    }
}
