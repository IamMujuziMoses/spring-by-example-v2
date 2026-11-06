# Job Restartability

This example demonstrates **job restartability** in Spring Batch.

It focuses on how Spring Batch handles failed job executions, how completed steps are skipped when a job is restarted, how execution context state is persisted, and how the same job instance can resume execution after a failure.

The example uses a JDBC-backed `JobRepository` and H2 database to demonstrate restartability across separate job executions.

## What This Example Demonstrates

- What job restartability means in Spring Batch.
- The relationship between `JobInstance` and `JobExecution`.
- The role of `JobRepository` in managing job metadata.
- Restarting failed jobs using `JobOperator`.
- Reusing identifying job parameters when restarting a job.
- Skipping steps that have already completed successfully.
- Persisting execution context data across job executions.
- Using the job-level `ExecutionContext` to maintain restart state.
- The difference between restarting a job and starting a new job instance.
- Preventing completed job executions from being restarted.
- Testing restartability using Spring Boot integration tests.

---

## What Is Job Restartability?

Job restartability is the ability to resume a failed or stopped Spring Batch job without repeating work that has already completed successfully.

A batch job may contain several steps. If one step fails, Spring Batch records the execution state so that the job can be restarted later.

When the job is restarted, previously completed steps are normally skipped, and execution resumes from the failed step.

For example:

```text
Batch Job
    │
    ▼
First Step
    │
    ▼
Second Step
    │
    ▼
Third Step
```

Suppose the first and second steps complete successfully, but the third step fails.

When the job is restarted:

```text
Batch Job Restart
    │
    ▼
First Step  ─────── Skipped
    │
    ▼
Second Step ────── Skipped
    │
    ▼
Third Step
    │
    ▼
Completed
```

This avoids unnecessarily repeating work that has already been completed.

---

## JobInstance and JobExecution

Understanding the difference between `JobInstance` and `JobExecution` is essential when working with job restartability.

### JobInstance

A `JobInstance` represents a logical execution of a job identified by its job name and identifying job parameters.

For example:

```text
Job Name: restartabilityJob

Parameters: run.id = abc123
```

Together, these identify a particular job instance.

Starting the same job with the same identifying parameters refers to the same logical job instance.

### JobExecution

A `JobExecution` represents one attempt to execute a particular job instance.

A job instance can have multiple job executions when it is restarted.

For example:

```text
JobInstance: restartabilityJob
    │
    ├── JobExecution 1
    │       │
    │       └── FAILED
    │
    └── JobExecution 2
            │
            └── COMPLETED
```

The two executions belong to the same job instance but have different execution IDs.

This distinction allows Spring Batch to maintain execution history while supporting job restarts.

---

## JobRepository

The `JobRepository` is responsible for storing and retrieving Spring Batch execution metadata.

It manages information about:

- Job instances.
- Job executions.
- Step executions.
- Job execution contexts.
- Step execution contexts.
- Execution statuses.
- Execution parameters.

Spring Batch uses this information to determine which steps have completed and where a failed job should resume.

This example uses a JDBC-backed repository.

The application enables JDBC job repository configuration:

```java
@SpringBootApplication
@EnableBatchProcessing
@EnableJdbcJobRepository
public class RestartabilityApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestartabilityApplication.class, args);
    }
}
```

The H2 database provides an embedded database for storing batch metadata.

```properties
spring.datasource.url=jdbc:h2:mem:restartabilitydb;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
```

The Spring Batch schema is initialized using:

```properties
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:org/springframework/batch/core/schema-h2.sql
```

A persistent repository is important because restartability depends on retaining execution metadata between job executions.

---

## Job Parameters and Job Identity

Spring Batch uses job parameters to identify job instances.

Consider:

```java
JobParameters jobParameters = new JobParametersBuilder().addString("run.id", UUID.randomUUID().toString()).toJobParameters();
```

The `run.id` parameter is identifying by default.

This means that a different value creates a different job instance.

For example:

```text
First Launch:
    run.id = abc123
    JobInstance = 1

Restart:
    run.id = abc123
    JobInstance = 1

New Launch:
    run.id = xyz789
    JobInstance = 2
```

The restart uses the same identifying parameters because it continues the original job instance.

A new launch with different identifying parameters creates a new job instance.

This distinction is important because Spring Batch uses job identity to determine whether an execution is a restart or a new job.

---

## JobOperator

Spring Batch provides `JobOperator` for operations such as starting and restarting jobs.

This example uses `JobOperator` rather than the deprecated `JobLauncher` API.

### Starting a Job

The initial execution is started using:

```java
JobExecution execution = jobOperator.start(restartabilityJob, jobParameters);
```

This creates the initial execution for the job instance.

### Restarting a Job

If the execution fails, it can be restarted using:

```java
JobExecution restartedExecution = jobOperator.restart(execution);
```

Spring Batch creates a new `JobExecution` for the same `JobInstance`.

The repository provides the execution metadata required to determine which steps need to run again.

### Restarting a Completed Job

A completed job execution cannot be restarted.

Attempting to restart it results in a `JobRestartException`.

This prevents an already completed job instance from being executed again as though it were an unfinished job.

If the same logical work needs to run again, a new job instance should be created using different identifying parameters.

---

## Steps and Restartability

This example defines a job containing two steps:

1. `firstStep`
2. `restartableStep`

The job configuration is:

```java
@Bean
public Job restartabilityJob(JobRepository jobRepository, Step firstStep, Step restartableStep) {

    return new JobBuilder("restartabilityJob", jobRepository).start(firstStep).next(restartableStep).build();
}
```

The first step completes successfully.

The second step deliberately fails on its first execution.

The expected execution sequence is:

```text
Initial Execution
    │
    ▼
firstStep
    │
    ▼
COMPLETED
    │
    ▼
restartableStep
    │
    ▼
FAILED
    │
    ▼
restartabilityJob
    │
    ▼
FAILED
```

After restarting:

```text
Restarted Execution
    │
    ▼
firstStep
    │
    ▼
SKIPPED
    │
    ▼
restartableStep
    │
    ▼
COMPLETED
    │
    ▼
restartabilityJob
    │
    ▼
COMPLETED
```

The first step does not execute again because it already completed successfully.

The second step executes again because its previous execution failed.

---

## ExecutionContext and Restart State

The `ExecutionContext` is used by Spring Batch to store state that needs to be maintained across executions.

Spring Batch provides execution contexts at different levels:

- Job execution context.
- Step execution context.

The job execution context is useful for sharing state between steps and maintaining state that needs to survive a job restart.

This example stores an attempt counter in the job execution context.

```java
private static final String ATTEMPT_COUNT = "restartability.attempt.count";
```

The tasklet retrieves and increments the counter:

```java
var jobExecutionContext = contribution.getStepExecution().getJobExecution().getExecutionContext();

int attempt = jobExecutionContext.getInt(ATTEMPT_COUNT, 0) + 1;

jobExecutionContext.putInt(ATTEMPT_COUNT, attempt);
```

The first execution stores:

```text
restartability.attempt.count = 1
```

The tasklet then deliberately fails:

```java
if (attempt == 1) {
    throw new IllegalStateException("Simulated failure for restartability demonstration.");
}
```

When the job is restarted, the persisted job execution context is restored.

The tasklet retrieves the previous attempt count and increments it:

```text
Previous attempt count = 1
Current attempt count  = 2
```

The tasklet then completes successfully.

This demonstrates how execution context data can be used to maintain restart state.

### Important Persistence Detail

Spring Batch manages execution context persistence through its `JobRepository`.

The tasklet updates the execution context, and Spring Batch persists the appropriate metadata as part of its execution lifecycle.

This example uses an in-memory H2 database, so the metadata survives separate job executions within the same application process, but it does not survive an application restart.

A production application would normally use a persistent database.

---

## FirstStepTasklet

The first tasklet represents a successfully completed step.

It increments an execution counter for testing purposes and returns `RepeatStatus.FINISHED`.

```java
@Override
public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

    executionCount.incrementAndGet();

    return RepeatStatus.FINISHED;
}
```

The counter is not used to implement restartability.

It exists to verify that Spring Batch does not execute the completed step again after a restart.

---

## RestartableTasklet

The second tasklet deliberately fails on its first execution and succeeds on its subsequent execution.

Its behavior is based on the persisted attempt counter.

```java
@Override
public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

    var jobExecutionContext = contribution.getStepExecution().getJobExecution().getExecutionContext();

    int attempt = jobExecutionContext.getInt(ATTEMPT_COUNT, 0) + 1;

    jobExecutionContext.putInt(ATTEMPT_COUNT, attempt);

    if (attempt == 1) {
        throw new IllegalStateException("Simulated failure for restartability demonstration.");
    }

    return RepeatStatus.FINISHED;
}
```

The expected behavior is:

| Execution           | Attempt Count | Result    |
|---------------------|--------------:|-----------|
| Initial execution   |             1 | FAILED    |
| Restarted execution |             2 | COMPLETED |

The tasklet's in-memory execution counter is used only for test assertions. The job execution context is responsible for preserving the actual restart state.

---

## Restarting a Failed Job

The integration tests demonstrate the complete restart process.

The initial execution is started:

```java
JobExecution firstExecution = jobOperator.start(restartabilityJob, jobParameters);
```

The first execution is expected to fail:

```java
assertThat(firstExecution.getStatus()).isEqualTo(BatchStatus.FAILED);
```

The failed execution is then restarted:

```java
JobExecution restartedExecution = jobOperator.restart(firstExecution);
```

The restarted execution should complete:

```java
assertThat(restartedExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
```

The restarted execution has a different execution ID but belongs to the same job instance:

```java
assertThat(restartedExecution.getId()).isNotEqualTo(firstExecution.getId());

assertThat(restartedExecution.getJobInstance().getInstanceId()).isEqualTo(firstExecution.getJobInstance().getInstanceId());
```

This demonstrates the relationship between job instances and job executions during a restart.

---

## Preventing Completed Steps from Executing Again

One of the most important restartability behaviors is that completed steps are not unnecessarily repeated.

The integration test verifies the execution count of the first tasklet:

```java
assertThat(firstStepTasklet.getExecutionCount()).isEqualTo(1);
```

After restarting the job, the counter should still be 1.

The second tasklet, however, should have executed twice:

```java
assertThat(restartableTasklet.getExecutionCount()).isEqualTo(2);
```

Conceptually:

```text
Initial Execution:
    firstStep       = 1 execution
    restartableStep = 1 execution

After Restart:
    firstStep       = 1 execution
    restartableStep = 2 executions
```

This verifies that Spring Batch resumes from the failed step rather than repeating the entire job.

---

## Testing Persisted Execution Context

The example also verifies that the attempt counter is actually persisted.

After the first execution fails, the test retrieves the execution again through the repository:

```java
JobExecution persistedExecution =
        jobRepository.getJobExecution(firstExecution.getId());
```

It then checks the stored attempt count:

```java
assertThat(persistedExecution.getExecutionContext()
        .getInt(RestartableTasklet.ATTEMPT_COUNT))
        .isEqualTo(1);
```

After restarting the job, the test retrieves the restarted execution and verifies that the count is now 2.

This is important because checking only the in-memory tasklet state would not prove that the state survived between job executions.

---

## Job Restartability vs Starting a New Job

Restarting a job and starting a new job instance are different operations.

### Restarting

A restart continues the same job instance.

```text
JobInstance 1
    │
    ├── Execution 1: FAILED
    │
    └── Execution 2: COMPLETED
```

The identifying job parameters remain the same.

### Starting a New Job

Starting a new job with different identifying parameters creates another job instance.

```text
JobInstance 1
    │
    └── Execution 1: FAILED

JobInstance 2
    │
    └── Execution 1: FAILED
```

The integration tests verify that different identifying parameters produce different job instances.

---

## Testing Restartability

The example uses JUnit Jupiter, AssertJ, and Spring Boot's test support.

The tests are organized into three classes.

### FirstStepTaskletTest

Tests the first tasklet:

- Verifies that execution returns `RepeatStatus.FINISHED`.
- Verifies that the execution counter is incremented.
- Verifies that repeated invocations increment the counter correctly.

### RestartableTaskletTest

Tests the restartable tasklet:

- Verifies that the first attempt throws the expected exception.
- Verifies that a subsequent attempt completes successfully.
- Verifies that an existing attempt count is incremented.

### RestartabilityJobConfigurationTest

Tests the complete job using the JDBC-backed repository:

- Verifies that completed steps are not executed again after a restart.
- Verifies that execution context state is persisted and restored.
- Verifies that the restarted job and its steps complete successfully.
- Verifies that a completed execution cannot be restarted.
- Verifies that different identifying parameters create a new job instance.

The integration tests are particularly important because restartability depends on the interaction between the job, steps, execution contexts, and repository.

---

## Important Testing Detail

The tasklet unit tests and job integration tests verify different responsibilities.

The unit tests verify the tasklet's behavior in isolation.

The integration tests verify the actual Spring Batch restart lifecycle.

For example, manually setting the attempt counter in a unit test:

```java
jobExecution.getExecutionContext().putInt(RestartableTasklet.ATTEMPT_COUNT, 1);
```

does not demonstrate persistence.

Persistence is verified by running the job, allowing it to fail, retrieving the execution through the JDBC-backed repository, and then restarting it.

This distinction ensures that the example tests actual restartability rather than simply testing conditional tasklet logic.

---

## Running the Example

From the repository root:

```bash
mvn -pl 07-spring-batch/restartability spring-boot:run
```

The application starts with the JDBC-backed Spring Batch repository configured.

The job is not automatically launched on application startup:

```properties
spring.batch.job.enabled=false
```

The job is launched programmatically through `JobOperator` in the integration tests.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 07-spring-batch/restartability test
```

To run the complete verification lifecycle:

```bash
mvn -pl 07-spring-batch/restartability clean verify
```

---

## Key Takeaways

- Job restartability allows failed or stopped jobs to resume execution.
- A `JobInstance` represents a logical job identified by its job name and identifying parameters.
- A `JobExecution` represents one attempt to execute a job instance.
- A single job instance can have multiple job executions.
- `JobRepository` stores the metadata required for job restartability.
- `JobOperator` provides operations for starting and restarting jobs.
- Restarting a failed job creates a new execution for the same job instance.
- Previously completed steps are normally skipped during a restart.
- The `ExecutionContext` can preserve state across job executions.
- A JDBC-backed repository is necessary for this example's persisted restart metadata.
- Different identifying parameters create a new job instance.
- A completed job execution cannot be restarted.
- Integration tests are essential for verifying actual restart behavior.

---

## Next Example

The next example will explore **Skip and Retry** in Spring Batch, including how to skip individual failed items, configure retry behavior, and handle exceptions during batch processing.
