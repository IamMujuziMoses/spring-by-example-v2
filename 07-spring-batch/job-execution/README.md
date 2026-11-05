# Job Execution and Metadata

This example demonstrates **job execution and metadata management** in Spring Batch.

It focuses on how Spring Batch launches jobs, creates job instances and job executions, records execution metadata, and exposes that metadata through an HTTP API.

The example uses a JDBC-backed `JobRepository` with an H2 in-memory database to demonstrate how Spring Batch persists and retrieves job execution information.

## What This Example Demonstrates

- What job execution means in Spring Batch.
- The relationship between `Job`, `JobInstance`, and `JobExecution`.
- The role of `JobRepository`.
- The role of `JobOperator` in launching jobs.
- Identifying job parameters and job instance identity.
- How Spring Batch records execution metadata.
- The relationship between `JobExecution` and `StepExecution`.
- Accessing job execution metadata through `JobRepository`.
- Exposing job execution information through REST endpoints.
- Handling requests for nonexistent job executions.
- Configuring a JDBC-backed `JobRepository`.
- Testing job execution and metadata using Spring Boot and `MockMvc`.

---

## What Is Job Execution?

A job execution represents one attempt to run a Spring Batch job.

A job defines the work to be performed, while a job execution records a particular attempt to perform that work.

For example, an application might define a job that processes customer records. Every time that job is launched, Spring Batch creates a job execution containing information about that particular run.

A simplified flow looks like:

```text
Job
 │
 ▼
Job Instance
 │
 ▼
Job Execution
 │
 ▼
Step Execution(s)
 │
 ▼
Execution Metadata
```

Spring Batch maintains this information so that applications can inspect job execution history, determine execution outcomes, and manage batch processing.

---

## Job, JobInstance, and JobExecution

These three concepts are central to understanding Spring Batch execution metadata.

### Job

A `Job` represents a batch process.

It defines the steps that Spring Batch must execute.

For example:

```java
@Bean
public Job jobExecutionMetadataJob(JobRepository jobRepository, Step processMetadataStep) {

    return new JobBuilder("jobExecutionMetadataJob", jobRepository).start(processMetadataStep).build();
}
```

The job is named `jobExecutionMetadataJob` and contains the `processMetadataStep` step.

Defining a job does not execute it. It only describes the batch process.

### JobInstance

A `JobInstance` represents a logical instance of a job.

Spring Batch identifies a job instance using:

- The job name.
- The job's identifying job parameters.

Conceptually:

```text
Job Name
   +
Identifying Job Parameters
   │
   ▼
Job Instance
```

Launching the same job with the same identifying parameters refers to the same job instance.

Launching it with different identifying parameters creates a different job instance.

### JobExecution

A `JobExecution` represents an individual attempt to execute a job instance.

A job instance can have multiple job executions, particularly when an earlier execution fails and the job is restarted.

For example:

```text
Job: customerImportJob
          │
          ▼
Job Instance: 1
          │
          ├── Job Execution: 1 (FAILED)
          │
          └── Job Execution: 2 (COMPLETED)
```

Both executions belong to the same logical job instance.

This distinction is important:

- `JobInstance` identifies the logical batch process and its identifying parameters.
- `JobExecution` records an individual attempt to execute that process.

---

## Job Parameters

Job parameters provide input values when launching a job.

They can influence how a job executes and determine whether a new job instance is created.

This example uses a UUID as an identifying parameter:

```java
JobParameters jobParameters = new JobParametersBuilder()
        .addJobParameter("run.id", UUID.randomUUID().toString(), String.class, true).toJobParameters();
```

The four arguments represent:

1. The parameter name: `run.id`.
2. The parameter value: a generated UUID.
3. The parameter type: `String.class`.
4. Whether the parameter is identifying: `true`.

Because `run.id` is identifying, every generated UUID produces a different job instance.

For example:

```text
First Launch
    run.id = 123
        │
        ▼
Job Instance 1

Second Launch
    run.id = 456
        │
        ▼
Job Instance 2
```

This makes it possible to launch the same job multiple times as separate logical instances.

---

## Identifying and Non-Identifying Parameters

Spring Batch distinguishes between identifying and non-identifying parameters.

### Identifying Parameters

Identifying parameters contribute to the identity of a job instance.

```java
.addJobParameter("run.id", UUID.randomUUID().toString(), String.class, true)
```

Changing an identifying parameter creates a different job instance.

### Non-Identifying Parameters

Non-identifying parameters do not contribute to the identity of a job instance.

For example:

```java
.addJobParameter("requested.by", "admin", String.class, false)
```

Changing this parameter does not, by itself, create a different job instance.

The distinction matters when designing job launches, particularly when restartability and job instance identity are important.

---

## JobRepository

The `JobRepository` is responsible for managing Spring Batch metadata.

It stores and retrieves information about:

- Job instances.
- Job executions.
- Step executions.
- Job parameters.
- Execution contexts.
- Execution statuses.

The job configuration uses the repository when building the job and step:

```java
@Bean
public Step processMetadataStep(JobRepository jobRepository, JobExecutionMetadataTasklet tasklet) {

    return new StepBuilder("processMetadataStep", jobRepository).tasklet(tasklet).build();
}
```

The job also receives the same repository:

```java
@Bean
public Job jobExecutionMetadataJob(JobRepository jobRepository, Step processMetadataStep) {

    return new JobBuilder("jobExecutionMetadataJob", jobRepository).start(processMetadataStep).build();
}
```

The repository provides the infrastructure Spring Batch uses to manage execution metadata.

### JDBC-backed JobRepository

This example explicitly configures a JDBC-backed repository:

```java
@SpringBootApplication
@EnableBatchProcessing
@EnableJdbcJobRepository
public class JobExecutionApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobExecutionApplication.class, args);
    }
}
```

The JDBC-backed repository stores batch metadata in relational database tables.

The example uses H2:

```properties
spring.datasource.url=jdbc:h2:mem:batchdb;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
```

The database schema is initialized using Spring Batch's H2 schema script:

```properties
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:org/springframework/batch/core/schema-h2.sql
```

The `@EnableJdbcJobRepository` annotation enables the JDBC-backed repository infrastructure.

The explicit JDBC configuration is important because the example needs a JDBC-backed repository rather than an in-memory, resourceless repository.

The H2 database is in-memory, so its data is not intended to survive application termination.

---

## JobOperator

The `JobOperator` provides operations for managing and controlling job executions.

This example uses it to launch the configured job:

```java
JobExecution jobExecution = jobOperator.start(jobExecutionMetadataJob, jobParameters);
```

The operation receives:

- The job to execute.
- The parameters used for that execution.

It returns a `JobExecution` containing information about the launched job.

The returned execution is then used to expose the execution metadata through the REST API.

In this example, the job completes synchronously before the controller returns its response.

---

## JobExecution Metadata

A `JobExecution` contains information about a particular job execution.

Some important metadata fields include:

| Field            | Description                                        |
|------------------|----------------------------------------------------|
| `id`             | Unique identifier of the job execution             |
| `jobInstance`    | Logical job instance associated with the execution |
| `status`         | Current execution status                           |
| `startTime`      | Time the execution started                         |
| `endTime`        | Time the execution ended                           |
| `exitStatus`     | Exit status of the execution                       |
| `jobParameters`  | Parameters supplied when launching the job         |
| `stepExecutions` | Step executions associated with the job execution  |

Common execution statuses include:

- `STARTING`
- `STARTED`
- `STOPPING`
- `STOPPED`
- `FAILED`
- `COMPLETED`
- `ABANDONED`
- `UNKNOWN`

The final status depends on how the job execution proceeds.

For this example, a successful execution has:

```text
Status: COMPLETED
Exit Status: COMPLETED
```

---

## StepExecution Metadata

A job can contain one or more steps.

Spring Batch creates a `StepExecution` for each step execution.

A `StepExecution` records information about the execution of an individual step.

Important metadata includes:

| Field              | Description                             |
|--------------------|-----------------------------------------|
| `id`               | Unique identifier of the step execution |
| `stepName`         | Name of the step                        |
| `status`           | Execution status                        |
| `startTime`        | Time the step started                   |
| `endTime`          | Time the step ended                     |
| `readCount`        | Number of items read                    |
| `writeCount`       | Number of items written                 |
| `commitCount`      | Number of successful commits            |
| `rollbackCount`    | Number of rollbacks                     |
| `executionContext` | Step-level execution context            |

The controller exposes these values as part of the job execution response.

For example:

```java
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

    return metadata;
}
```

The example also exposes a custom value stored in the step execution context.

---

## ExecutionContext

Spring Batch provides `ExecutionContext` for storing execution-related data.

An execution context is associated with a job execution or step execution and can hold values needed during processing.

This example uses the step execution context to record simple processing metadata.

The tasklet stores:

- `metadata.processed.records`
- `metadata.processing.status`

Conceptually:

```text
Step Execution
      │
      ▼
ExecutionContext
      │
      ├── metadata.processed.records = 5
      │
      └── metadata.processing.status = COMPLETED
```

The controller retrieves the processed record count:

```java
metadata.put("processedRecords", stepExecution.getExecutionContext().getInt("metadata.processed.records", 0));
```

This demonstrates how application-specific metadata can be stored alongside Spring Batch's execution information.

The value of five is illustrative metadata recorded by the tasklet. It does not represent five records read or written by a chunk-oriented step.

---

## REST API

This example exposes job execution metadata through a REST controller.

The controller provides two endpoints.

### Launch a Job

```http
POST /jobs/executions
```

This endpoint:

1. Generates a unique identifying job parameter.
2. Creates the job parameters.
3. Launches the job using `JobOperator`.
4. Retrieves the resulting `JobExecution`.
5. Returns the execution metadata.

A successful response contains information similar to:

```json
{
  "executionId": 1,
  "jobName": "jobExecutionMetadataJob",
  "jobInstanceId": 1,
  "status": "COMPLETED",
  "startTime": "2026-11-04T10:00:00",
  "endTime": "2026-11-04T10:00:01",
  "exitStatus": "COMPLETED",
  "steps": [
    {
      "stepExecutionId": 1,
      "stepName": "processMetadataStep",
      "status": "COMPLETED",
      "readCount": 0,
      "writeCount": 0,
      "commitCount": 1,
      "rollbackCount": 0,
      "processedRecords": 5
    }
  ]
}
```

The identifiers and timestamps are illustrative. Actual values are generated by the repository when the job executes.

### Retrieve an Existing Execution

```http
GET /jobs/executions/{executionId}
```

This endpoint retrieves an existing job execution using its execution ID.

The controller uses the `JobRepository` to retrieve the execution:

```java
JobExecution jobExecution = jobRepository.getJobExecution(executionId);
```

It then converts the execution and its step executions into a response containing the available metadata.

### Handling Unknown Executions

If the requested execution does not exist, the endpoint returns HTTP `404 Not Found`.

With the JDBC-backed repository, looking up a nonexistent execution can result in an `EmptyResultDataAccessException`.

The controller handles this case and translates it into an appropriate HTTP response.

Conceptually:

```text
GET /jobs/executions/999999
             │
             ▼
       JobRepository
             │
             ▼
     Execution Not Found
             │
             ▼
       HTTP 404
```

This prevents an internal data-access exception from being exposed as an unhandled server error.

---

## Understanding the Execution Flow

The complete flow of this example is:

```text
HTTP POST /jobs/executions
             │
             ▼
      Job Parameters
             │
             ▼
        JobOperator
             │
             ▼
       Job Execution
             │
             ▼
       Job Instance
             │
             ▼
    processMetadataStep
             │
             ▼
   JobExecutionMetadataTasklet
             │
             ▼
      ExecutionContext
             │
             ▼
      JobRepository
             │
             ▼
    Execution Metadata
             │
             ▼
       HTTP Response
```

The execution metadata can subsequently be retrieved using:

```text
HTTP GET /jobs/executions/{executionId}
```

---

## JobInstance vs JobExecution vs StepExecution

These concepts can be summarized as:

| Concept            | Responsibility                                                     |
|--------------------|--------------------------------------------------------------------|
| `Job`              | Defines the batch process                                          |
| `JobInstance`      | Identifies a logical job using its name and identifying parameters |
| `JobExecution`     | Represents an attempt to execute a job instance                    |
| `StepExecution`    | Represents an attempt to execute an individual step                |
| `JobRepository`    | Stores and retrieves batch execution metadata                      |
| `ExecutionContext` | Stores execution-related data associated with a job or step        |

Understanding these distinctions is essential when working with job monitoring, execution history, and restartability.

---

## Running the Example

From the repository root:

```bash
mvn -pl 07-spring-batch/job-execution spring-boot:run
```

The application starts with the H2 in-memory database and JDBC-backed Spring Batch repository configured.

Launch a job:

```bash
curl -X POST http://localhost:8080/jobs/executions
```

Retrieve an execution using the returned execution ID:

```bash
curl http://localhost:8080/jobs/executions/1
```

The actual execution ID will depend on the executions recorded in the database.

---

## Running the Tests

Run the tests from the module:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 07-spring-batch/job-execution test
```

To run the full module verification:

```bash
mvn -pl 07-spring-batch/job-execution clean verify
```

---

## Key Takeaways

- A `Job` defines a batch process.
- A `JobInstance` represents a logical instance of a job.
- Identifying job parameters determine job instance identity.
- A `JobExecution` represents an individual attempt to execute a job instance.
- A job instance can have multiple job executions.
- A `StepExecution` records the execution details of an individual step.
- `JobRepository` manages Spring Batch execution metadata.
- `JobOperator` provides operations for launching and managing jobs.
- `ExecutionContext` stores job-level or step-level execution data.
- A JDBC-backed repository stores execution metadata in relational database tables.
- Job execution metadata can be exposed through REST endpoints.
- Missing executions should be handled appropriately rather than exposing internal data-access exceptions.

---

## Next Example

The next example will explore **Restartability** in Spring Batch, including how failed jobs can be restarted, how execution state is maintained, and how Spring Batch uses job instances and execution contexts to support restarting batch processes.
