# Job Parameters

This example demonstrates **job parameters** in Spring Batch.

It focuses on how runtime parameters are supplied to a batch job, how identifying and non-identifying parameters affect job instance identity, and how parameters can be accessed inside a step using step-scoped components.

## What This Example Demonstrates

- What job parameters are in Spring Batch.
- Supplying parameters when launching a job.
- `JobParameters` and `JobParametersBuilder`.
- Identifying and non-identifying job parameters.
- How identifying parameters determine a `JobInstance`.
- Accessing job parameters inside a step.
- Using `@StepScope` for runtime parameter injection.
- Using `@Value` to inject job parameters.
- Storing values in the step execution context.
- Testing job execution with supplied parameters.

---

## What Are Job Parameters?

Job parameters are values supplied to a Spring Batch job when it is launched.

They allow a job to receive runtime information without hardcoding those values into the job configuration.

For example, a report generation job might need:

- The date for which the report should be generated.
- The desired report format.
- The user or process requesting the report.

Instead of hardcoding these values, they can be supplied when the job is launched.

Conceptually:

```text
Job Launch
    |
    v
Job Parameters
    |
    v
Job Instance
    |
    v
Job Execution
    |
    v
Step Execution
    |
    v
Job Logic
```

This makes batch jobs reusable because the same job configuration can process different input values on different executions.

---

## JobParameters

Spring Batch represents job parameters using the `JobParameters` class.

A `JobParameters` object contains the parameters supplied when a job is launched.

Parameters have:

- A name.
- A value.
- A data type.
- An identifying flag.

The identifying flag determines whether a parameter contributes to the identity of a job instance.

The example uses the following parameters:

| Parameter       | Value        | Type     | Identifying |
|-----------------|--------------|----------|-------------|
| `report.date`   | `2026-11-03` | `String` | Yes         |
| `report.format` | `CSV`        | `String` | Yes         |
| `requested.by`  | `batch-user` | `String` | No          |

These parameters provide the information needed by the report generation job.

---

## Creating Job Parameters

The example uses `JobParametersBuilder` to construct the parameters.

```java
public static JobParameters createJobParameters() {
    return new JobParametersBuilder()
            .addJobParameter("report.date", "2026-11-03", String.class, true)
            .addJobParameter("report.format", "CSV", String.class, true)
            .addJobParameter("requested.by", "batch-user", String.class, false)
            .toJobParameters();
}
```

`JobParametersBuilder` provides a convenient way to create a `JobParameters` object.

The `addJobParameter` method accepts:

1. The parameter name.
2. The parameter value.
3. The parameter type.
4. Whether the parameter is identifying.

The `toJobParameters()` method builds the final `JobParameters` instance.

### Typed Parameters

Spring Batch supports typed job parameters.

The type is important because parameters are not limited to strings. Depending on the use case, a job can receive values such as:

- `String`
- `Long`
- `Double`
- `LocalDate`
- `LocalDateTime`
- `Date`

Using typed parameters allows a job to receive values in a form appropriate to its processing requirements.

---

## Identifying and Non-Identifying Parameters

One of the most important concepts in Spring Batch job parameters is the distinction between identifying and non-identifying parameters.

### Identifying Parameters

Identifying parameters contribute to the identity of a `JobInstance`.

For example:

```java
.addJobParameter("report.date","2026-11-03",String.class,true)
```

The `true` value indicates that `report.date` is an identifying parameter.

If the same job is launched with the same identifying parameter values, Spring Batch considers it the same job instance.

Changing an identifying parameter creates a different job instance.

For example:

```text
Job: reportGenerationJob

Execution 1:
    report.date = 2026-11-03
    report.format = CSV

Execution 2:
    report.date = 2026-11-04
    report.format = CSV
```

Because the report date changed, these executions identify different job instances.

### Non-Identifying Parameters

Non-identifying parameters do not contribute to the identity of a `JobInstance`.

For example:

```java
.addJobParameter("requested.by","batch-user",String.class,false)
```

The `false` value indicates that `requested.by` is non-identifying.

Changing this parameter does not change the identity of the job instance.

For example:

```text
Job: reportGenerationJob

Execution 1:
    report.date = 2026-11-03
    report.format = CSV
    requested.by = batch-user

Execution 2:
    report.date = 2026-11-03
    report.format = CSV
    requested.by = another-user
```

Both launches have the same identifying parameter values.

Therefore, they refer to the same `JobInstance`, even though the non-identifying parameter differs.

An important consequence is that a job instance that has already completed cannot simply be executed again with the same identifying parameters. Spring Batch prevents another execution of an already completed job instance.

To create a new job instance, at least one identifying parameter must change.

---

## JobInstance and JobExecution

Understanding the difference between `JobInstance` and `JobExecution` is essential when working with job parameters.

### JobInstance

A `JobInstance` represents a logical execution of a job identified by:

- The job name.
- Its identifying job parameters.

For example:

```text
Job Name: reportGenerationJob

Identifying Parameters:
    report.date = 2026-11-03
    report.format = CSV
```

These values identify a particular job instance.

### JobExecution

A `JobExecution` represents an attempt to execute a job instance.

A job instance can have multiple executions when previous executions did not complete successfully and the job is eligible for another attempt.

Conceptually:

```text
JobInstance
     |
     +---- JobExecution 1 (FAILED)
     |
     +---- JobExecution 2 (COMPLETED)
```

The identifying parameters remain the same across executions of the same job instance.

Non-identifying parameters can provide additional information without changing the job instance identity.

---

## Accessing Job Parameters

The example accesses job parameters inside a tasklet.

Spring Batch makes job parameters available through the job execution context.

The configuration uses `@Value`:

```java
@Value("#{jobParameters['report.date']}")
String reportDate
```

The expression:

```java
#{jobParameters['report.date']}
```

retrieves the value of the `report.date` job parameter.

The example injects three parameters:

```java
@Bean
@StepScope
public Tasklet generateReportTasklet(
        @Value("#{jobParameters['report.date']}") String reportDate,
        @Value("#{jobParameters['report.format']}") String reportFormat,
        @Value("#{jobParameters['requested.by']}") String requestedBy) {
    // Tasklet implementation
}
```

This allows the tasklet to use values supplied when the job is launched.

The tasklet does not need to know where the values originated or how they were constructed.

---

## Why `@StepScope`?

The example uses:

```java
@StepScope
```

on the tasklet bean.

Step scope delays the creation of the scoped bean until the step executes.

This is important because job parameters are available in the context of a running job execution.

Without step scope, Spring would normally attempt to resolve the injected values while creating the application context, before the job has been launched with its parameters.

With step scope, the values can be resolved at execution time.

Conceptually:

```text
Application Starts
       |
       v
Job Configuration Created
       |
       v
Job Launched with Parameters
       |
       v
Step Starts
       |
       v
Step-Scoped Tasklet Created
       |
       v
Job Parameters Injected
       |
       v
Tasklet Executes
```

This is particularly useful when a step needs runtime values such as file paths, dates, processing options, or identifiers.

---

## Using Job Parameters in the Tasklet

The tasklet receives the injected parameters and uses them during execution.

```java
return (contribution, chunkContext) -> {
    LOGGER.info("Generating report for date: {}", reportDate);
    LOGGER.info("Report format: {}", reportFormat);
    LOGGER.info("Requested by: {}", requestedBy);

    return RepeatStatus.FINISHED;
};
```

In this example, the tasklet logs the supplied report details.

The actual report generation is intentionally left out because the purpose of this example is to demonstrate how job parameters are supplied and accessed.

---

## Step Execution Context

The example also stores the received values in the step execution context:

```java
var executionContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();

executionContext.putString("report.date", reportDate);
executionContext.putString("report.format", reportFormat);
executionContext.putString("requested.by", requestedBy);
```

The `ExecutionContext` provides a mechanism for storing state associated with a job or step execution.

Unlike job parameters, execution context values are not supplied when the job is launched. They are created and updated during job execution.

This distinction is important:

| Job Parameters                        | Execution Context                     |
|---------------------------------------|---------------------------------------|
| Supplied when a job is launched       | Updated during job execution          |
| Used to provide input to a job        | Used to maintain execution state      |
| Can be identifying or non-identifying | Not used to identify a job instance   |
| Available to job and step components  | Associated with job or step execution |

In this example, the execution context is used to make the parameter values available for verification after the step completes.

---

## Job Execution

The test launches the job using `JobOperator`:

```java
JobExecution jobExecution = jobOperator.start(reportGenerationJob, jobParameters);
```

The job parameters are supplied as part of the launch operation.

The `JobOperator` coordinates the launch and returns the resulting `JobExecution`.

The example disables automatic job launching in its test configuration:

```java
@SpringBootTest(properties = "spring.batch.job.enabled=false")
```

This allows the test to launch the job explicitly with the parameters it wants to verify.

---

## Testing Job Parameters

The example uses Spring Boot test support and JUnit Jupiter to verify job execution.

### Launching with Parameters

The test creates the parameters:

```java
var jobParameters = JobParametersConfiguration.createJobParameters();
```

It then launches the job:

```java
JobExecution jobExecution = jobOperator.start(reportGenerationJob, jobParameters);
```

### Verifying Job Completion

The test verifies that the job completes successfully:

```java
assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
```

### Verifying Step Execution

The test verifies that the expected step executes successfully:

```java
assertThat(stepExecution.getStepName()).isEqualTo("generateReportStep");
assertThat(stepExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
```

### Verifying Parameter Values

The test retrieves the step execution context:

```java
var executionContext = stepExecution.getExecutionContext();
```

It then verifies that the supplied values were stored correctly:

```java
assertThat(executionContext.getString("report.date")).isEqualTo("2026-11-03");
assertThat(executionContext.getString("report.format")).isEqualTo("CSV");
assertThat(executionContext.getString("requested.by")).isEqualTo("batch-user");
```

This confirms that the parameters were successfully supplied, injected into the tasklet, and used during execution.

---

## Job Parameters and Restartability

Job parameters are closely related to job restartability.

Spring Batch uses the job name and identifying parameters to determine which job instance is being executed.

When a job fails, it may be possible to restart that same job instance using the same identifying parameters.

For example:

```text
JobInstance: reportGenerationJob
    report.date = 2026-11-03
    report.format = CSV

JobExecution 1: FAILED
JobExecution 2: COMPLETED
```

The second execution is another attempt to complete the same job instance.

However, a completed job instance cannot simply be launched again with the same identifying parameters.

A new identifying parameter value is needed to create a new job instance.

Restartability, execution state management, and failure recovery are explored in more detail in separate Spring Batch examples.

---

## Running the Example

From the repository root:

```bash
mvn -pl 07-spring-batch/job-parameters spring-boot:run
```

The Spring Boot application starts with the configured report generation job.

---

## Running the Tests

Run the tests from the module:

```bash
mvn -pl 07-spring-batch/job-parameters test
```

Or run the complete Maven verification:

```bash
mvn -pl 07-spring-batch/job-parameters clean verify
```

---

## Key Takeaways

- Job parameters provide runtime input to Spring Batch jobs.
- `JobParameters` represents the parameters supplied during job launch.
- `JobParametersBuilder` provides a convenient way to construct job parameters.
- Job parameters can have different data types.
- Identifying parameters contribute to the identity of a `JobInstance`.
- Non-identifying parameters do not contribute to job instance identity.
- A `JobInstance` is identified by its job name and identifying parameters.
- A `JobExecution` represents an attempt to execute a job instance.
- `@StepScope` allows job parameters to be injected when a step executes.
- `@Value` can retrieve individual job parameters.
- `ExecutionContext` stores state created or updated during job execution.
- Job parameters play an important role in job restartability.

---

## Next Example

The next example will explore **Item Readers** in Spring Batch, including how batch jobs read input data using the `ItemReader` abstraction.
