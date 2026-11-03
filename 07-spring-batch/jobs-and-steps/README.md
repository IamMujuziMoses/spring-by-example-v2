
# Spring Batch Jobs and Steps

This example demonstrates how to define and configure **Spring Batch jobs and steps** using Spring Boot and Java configuration.

It focuses on how a batch job represents an entire processing workflow, how individual steps represent separate phases of that workflow, and how multiple steps can be combined to execute sequentially.

The example implements a simple report generation job consisting of two steps:

1. Prepare the report.
2. Generate the report.

It also demonstrates how to execute the job and verify its execution using Spring Batch's job execution infrastructure.

## What This Example Demonstrates

- What Spring Batch is and when it is useful.
- What a Spring Batch job represents.
- What a Spring Batch step represents.
- The relationship between jobs and steps.
- Defining jobs using Java configuration.
- Defining steps using `StepBuilder`.
- Defining jobs using `JobBuilder`.
- Combining multiple steps into a single job.
- Configuring sequential step execution.
- Understanding `JobRepository`.
- Understanding `JobExecution`.
- Understanding `StepExecution`.
- Executing jobs using `JobOperator`.
- Testing job and step execution with Spring Boot testing support.

---

## What Is Spring Batch?

Spring Batch is a framework for developing robust batch processing applications.

Batch processing involves executing a series of operations on a collection of data without requiring an individual request for every operation.

It is commonly used for tasks such as:

- Importing data from external systems.
- Processing large files.
- Generating reports.
- Exporting data.
- Synchronizing information between systems.
- Processing scheduled business operations.
- Performing periodic data maintenance.

A typical batch processing workflow looks like:

```text
Batch Application
       |
       v
      Job
       |
       v
     Steps
       |
       v
   Processing
       |
       v
    Results
```

Spring Batch provides infrastructure for defining, executing, monitoring, and managing these batch workflows.

---

## What Is a Job?

A `Job` represents an entire batch processing workflow.

It defines the sequence of operations that Spring Batch should execute to complete a particular batch process.

A job consists of one or more steps.

For example, a report generation job might contain:

```text
reportGenerationJob
        |
        v
prepareReportStep
        |
        v
generateReportStep
```

The job coordinates the execution of its steps.

A job can contain a single step or multiple steps, depending on the complexity of the processing workflow.

In this example, `reportGenerationJob` represents the complete report generation process.

---

## What Is a Step?

A `Step` represents an independent phase of a batch job.

Each step performs a specific part of the overall processing workflow.

For example, a report generation process can be divided into:

1. Preparing the information required for the report.
2. Generating the report.

Each operation is represented by a separate step.

Conceptually:

```text
reportGenerationJob
        |
        +----------------------+
        |                      |
        v                      v
prepareReportStep       generateReportStep
```

Separating a job into steps makes batch workflows easier to understand, configure, test, and maintain.

A step can use different processing approaches depending on the requirements.

Spring Batch supports two major step-processing styles:

- Tasklet-oriented processing.
- Chunk-oriented processing.

This example uses tasklet-oriented steps to keep the focus on job and step composition.

Chunk-oriented processing is covered separately in the dedicated Item Readers, Item Processors, Item Writers, and Chunk Processing examples.

---

## Relationship Between Jobs and Steps

A job is the overall batch process, while steps represent the individual phases that make up that process.

The relationship can be summarized as:

```text
Job
 |
 +-- Step 1
 |
 +-- Step 2
 |
 +-- Step 3
 |
 +-- ...
```

Spring Batch executes the steps according to the flow configured in the job.

A job can contain multiple steps, and each step can be responsible for a different operation.

For example, a data import job could contain:

```text
dataImportJob
      |
      v
readSourceFileStep
      |
      v
validateRecordsStep
      |
      v
importRecordsStep
      |
      v
generateSummaryStep
```

This separation allows each phase to have a clear responsibility.

---

## Defining Steps with StepBuilder

Spring Batch provides `StepBuilder` for creating step definitions.

In this example, the first step is defined as:

```java
@Bean
public Step prepareReportStep(JobRepository jobRepository) {
    return new StepBuilder("prepareReportStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                LOGGER.info("Preparing the report...");
                return RepeatStatus.FINISHED;
            }).build();
}
```

The `StepBuilder` constructor receives:

- The name of the step.
- The `JobRepository` used to manage batch execution metadata.

The `tasklet()` method configures the operation performed by the step.

The `build()` method creates the configured step.

The second step follows the same approach:

```java
@Bean
public Step generateReportStep(JobRepository jobRepository) {
    return new StepBuilder("generateReportStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                LOGGER.info("Generating the report...");
                return RepeatStatus.FINISHED;
            }).build();
}
```

Both steps are declared as Spring beans so they can be injected into the job configuration.

---

## Understanding Tasklet Steps

A tasklet represents a unit of work performed by a step.

It is useful for operations that do not naturally fit into the item-oriented processing model.

Examples include:

- Creating directories.
- Moving files.
- Executing a stored procedure.
- Preparing resources.
- Performing a simple operation before another step begins.

In this example, tasklets are used to represent report preparation and report generation.

A tasklet is configured using:

```java
.tasklet((contribution, chunkContext) -> {
    LOGGER.info("Preparing the report...");
    return RepeatStatus.FINISHED;
})
```

The tasklet receives two arguments:

- `StepContribution`: provides access to the contribution being made by the current step execution.
- `ChunkContext`: provides access to the context associated with the current chunk or tasklet execution.

The tasklet returns a `RepeatStatus` indicating whether execution should continue.

### RepeatStatus

Spring Batch provides `RepeatStatus` to control tasklet repetition.

The two main values are:

| Value         | Meaning                               |
|---------------|---------------------------------------|
| `FINISHED`    | The tasklet has completed its work.   |
| `CONTINUABLE` | The tasklet should be executed again. |

This example returns:

```java
return RepeatStatus.FINISHED;
```

This indicates that the tasklet has completed its work and the step can finish.

Tasklet configuration and more advanced tasklet behavior are explored in the dedicated Tasklets example.

---

## Defining a Job with JobBuilder

Spring Batch provides `JobBuilder` for creating job definitions.

The example defines the report generation job as:

```java
@Bean
public Job reportGenerationJob(JobRepository jobRepository, Step prepareReportStep, Step generateReportStep) {

    return new JobBuilder("reportGenerationJob", jobRepository).start(prepareReportStep).next(generateReportStep).build();
}
```

The `JobBuilder` constructor receives:

- The name of the job.
- The `JobRepository` used to manage job execution metadata.

The job then defines its execution flow.

### start()

The `start()` method identifies the first step in the job.

```java
.start(prepareReportStep)
```

This tells Spring Batch that `prepareReportStep` should execute first.

### next()

The `next()` method defines the next step in the job.

```java
.next(generateReportStep)
```

This tells Spring Batch that `generateReportStep` should execute after the preparation step completes successfully.

### build()

The `build()` method creates the job definition.

```java
.build();
```

The resulting job contains both steps and their execution order.

---

## Sequential Step Execution

The example configures two steps to execute sequentially.

The execution flow is:

```text
reportGenerationJob
        |
        v
prepareReportStep
        |
        v
generateReportStep
        |
        v
       END
```

The first step prepares the report.

After it completes successfully, Spring Batch proceeds to the second step.

The second step generates the report.

When both steps complete successfully, the job completes.

The application logs demonstrate this sequence:

```text
Preparing the report...
Generating the report...
```

Sequential execution is useful when one phase depends on the successful completion of an earlier phase.

For example, a data import process might need to validate a file before importing its records.

If a step fails, Spring Batch does not simply continue executing subsequent steps as though the failed step had succeeded.

More advanced conditional flows, transitions, and failure handling can be explored separately.

---

## Understanding JobRepository

`JobRepository` is an important part of Spring Batch infrastructure.

It provides access to the metadata associated with jobs and their executions.

It is used by Spring Batch to manage information such as:

- Job instances.
- Job executions.
- Step executions.
- Execution status.
- Execution context.
- Job parameters.

The job and step builders receive a `JobRepository`:

```java
new JobBuilder("reportGenerationJob", jobRepository)
```

```java
new StepBuilder("prepareReportStep", jobRepository)
```

This allows Spring Batch to coordinate and manage their executions.

The example relies on Spring Boot's Spring Batch infrastructure configuration rather than manually creating the repository.

---

## Understanding JobExecution

A `JobExecution` represents an individual attempt to execute a job.

It contains information about the execution, including:

- The job being executed.
- The execution status.
- The job parameters.
- The start time.
- The end time.
- The step executions associated with the job.

A job definition and a job execution are different concepts.

The `Job` defines what should execute.

The `JobExecution` represents a particular attempt to execute that job.

Conceptually:

```text
Job Definition
      |
      v
Job Execution
      |
      +-- Step Execution 1
      |
      +-- Step Execution 2
```

A job can have multiple executions under different circumstances.

For example, a job can be launched again with different identifying job parameters.

Restarting failed executions and managing job instances are explored in the dedicated Job Execution and Restartability examples.

---

## Understanding StepExecution

A `StepExecution` represents an individual attempt to execute a step.

It provides information about that step's execution.

Examples include:

- The step name.
- The execution status.
- The start time.
- The end time.
- Read counts.
- Write counts.
- Commit counts.
- Rollback counts.

A job execution contains the step executions associated with that particular job execution.

For this example:

```text
reportGenerationJob
        |
        +-- prepareReportStep
        |       |
        |       +-- StepExecution
        |
        +-- generateReportStep
                |
                +-- StepExecution
```

The test uses the step executions to verify that both configured steps completed successfully.

---

## Executing a Job with JobOperator

Spring Batch provides `JobOperator` for operating and managing jobs.

In this example, it is used to launch the report generation job.

The test creates job parameters:

```java
var jobParameters = new JobParametersBuilder()
        .addString("run.id", UUID.randomUUID().toString())
        .toJobParameters();
```

The unique value ensures that each test execution uses a different set of identifying parameters.

The job is then launched:

```java
JobExecution jobExecution = jobOperator.start(reportGenerationJob, jobParameters);
```

The `start(Job, JobParameters)` method starts a new job instance using the supplied job definition and parameters.

It returns a `JobExecution` that can be inspected to verify the outcome.

This example uses the Spring Batch 6 API.

---

## Job Parameters

Job parameters are values supplied when launching a job.

They can be used to influence how a job executes.

For example, a report generation job might receive:

- A reporting date.
- A source file location.
- A customer identifier.
- A processing mode.

Conceptually:

```text
reportGenerationJob
        |
        +-- reportDate = 2026-11-03
        |
        +-- outputFormat = CSV
        |
        +-- customerId = 1001
```

Spring Batch uses job parameters when identifying job instances.

In this example, a unique `run.id` parameter is supplied by the test to avoid reusing the same completed job instance when the test is executed repeatedly.

The example does not demonstrate parameter-driven business processing. That is covered in the dedicated Job Parameters example.

---

## Automatic Job Execution

Spring Boot can automatically launch a single configured Spring Batch job when the application starts.

This example uses that behavior when running the application.

The application starts, Spring Boot discovers the configured job, and the report generation job is launched.

The execution flow is:

```text
Start Spring Boot Application
             |
             v
Discover Configured Job
             |
             v
Launch reportGenerationJob
             |
             v
     prepareReportStep
             |
             v
     generateReportStep
             |
             v
       Job Completed
```

The test disables automatic job execution:

```java
@SpringBootTest(properties = "spring.batch.job.enabled=false")
```

This allows the test to launch the job explicitly using `JobOperator` and inspect the resulting execution.

---

## Testing Jobs and Steps

The example uses Spring Boot's testing support and JUnit Jupiter.

The integration test verifies that the job executes both steps successfully.

### Launching the Job

The test launches the job with unique parameters:

```java
JobExecution jobExecution = jobOperator.start(reportGenerationJob, jobParameters);
```

This executes the configured job through Spring Batch.

### Verifying Job Completion

The test verifies the overall job status:

```java
assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
```

This confirms that the job completed successfully.

### Verifying Step Execution

The test retrieves the names of the executed steps:

```java
var stepNames = jobExecution.getStepExecutions().stream().map(stepExecution -> stepExecution.getStepName()).toList();
```

It then verifies that both steps executed in the configured order:

```java
assertThat(stepNames).containsExactly("prepareReportStep","generateReportStep");
```

### Verifying Step Completion

Finally, the test verifies that every step completed successfully:

```java
assertThat(jobExecution.getStepExecutions())
        .allSatisfy(stepExecution ->
                assertThat(stepExecution.getStatus())
                        .isEqualTo(BatchStatus.COMPLETED)
        );
```

Together, these assertions verify the complete execution of the report generation job.

---

## Job Execution Flow

The complete example can be summarized as:

```text
Spring Boot Application
          |
          v
BatchJobConfiguration
          |
          v
reportGenerationJob
          |
          v
  prepareReportStep
          |
          v
  generateReportStep
          |
          v
    JobExecution
          |
          v
      COMPLETED
```

The job contains two steps, and each step performs a simple tasklet operation.

Spring Batch manages the execution and provides metadata that can be inspected after the job completes.

---

## Important Concepts

### Job vs Step

| Concept         | Responsibility                                       |
|-----------------|------------------------------------------------------|
| `Job`           | Represents the complete batch processing workflow.   |
| `Step`          | Represents an individual phase of the workflow.      |
| `JobExecution`  | Represents an attempt to execute a job.              |
| `StepExecution` | Represents an attempt to execute a step.             |
| `JobRepository` | Manages batch execution metadata.                    |
| `JobOperator`   | Provides operations for launching and managing jobs. |

### JobBuilder vs StepBuilder

| Builder       | Responsibility                                                     |
|---------------|--------------------------------------------------------------------|
| `JobBuilder`  | Creates job definitions and configures their execution flow.       |
| `StepBuilder` | Creates step definitions and configures their processing behavior. |

These builders provide a programmatic way to configure Spring Batch jobs and steps using Java configuration.

---

## Running the Example

From the repository root:

```bash
mvn -pl 07-spring-batch/jobs-and-steps spring-boot:run
```

The application starts and automatically executes the configured report generation job.

The expected application log messages include:

```text
Preparing the report...
Generating the report...
```

The job completes after both steps finish successfully.

---

## Running the Tests

Run the tests from the example directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 07-spring-batch/jobs-and-steps test
```

Run the complete Maven verification lifecycle:

```bash
mvn -pl 07-spring-batch/jobs-and-steps clean verify
```

The integration test verifies:

- Successful job execution.
- Execution of both configured steps.
- The configured step execution order.
- Successful completion of every step.

---

## Key Takeaways

- Spring Batch is a framework for developing batch processing applications.
- A job represents an entire batch processing workflow.
- A job consists of one or more steps.
- A step represents an individual phase of a job.
- `JobBuilder` is used to define jobs.
- `StepBuilder` is used to define steps.
- `start()` defines the first step in a job.
- `next()` defines subsequent steps.
- Steps can execute sequentially as part of a job.
- Tasklets provide a way to implement simple step-oriented operations.
- `JobRepository` manages batch execution metadata.
- `JobExecution` represents an attempt to execute a job.
- `StepExecution` represents an attempt to execute a step.
- `JobOperator` provides an API for launching and managing jobs.
- Job parameters can be supplied when launching a job.
- Spring Boot can automatically launch configured jobs at application startup.
- Spring Batch execution can be tested by inspecting job and step execution metadata.

---

## Next Example

The next example will explore **Spring Batch Job Parameters**, demonstrating how parameters are supplied when launching jobs, how they are accessed during execution, and how they influence job instance identification.
