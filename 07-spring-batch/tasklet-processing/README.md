# Tasklet Processing

This example demonstrates **tasklet processing** in Spring Batch.

It focuses on how Spring Batch executes a tasklet as a single unit of work, how a tasklet interacts with the step execution context, and how tasklet-based steps differ from chunk-oriented processing.

The example uses a simple order cleanup operation to demonstrate executing a tasklet, recording execution information, and verifying the result through unit and integration tests.

## What This Example Demonstrates

- What tasklet processing means in Spring Batch.
- The `Tasklet` interface.
- The `execute()` method and its parameters.
- The role of `StepContribution` and `ChunkContext`.
- The `RepeatStatus.FINISHED` return value.
- Creating a tasklet-based step with `StepBuilder`.
- Accessing the step's `ExecutionContext`.
- Storing execution information in the `ExecutionContext`.
- Configuring and launching a Spring Batch job.
- Testing tasklet behavior with unit and integration tests.

---

## What Is Tasklet Processing?

Tasklet processing is a Spring Batch programming model for executing a specific operation as a unit of work.

Unlike chunk-oriented processing, which repeatedly reads, processes, and writes items in groups, a tasklet executes a task through its `execute()` method.

Tasklets are useful for operations that do not naturally fit into an item-based processing pipeline.

Examples include:

- Cleaning up expired records.
- Moving or archiving files.
- Executing database maintenance operations.
- Preparing data before a batch job begins.
- Sending notifications.
- Performing a one-time administrative operation.

A simplified tasklet execution flow looks like:

```text
Job
 │
 ▼
Step
 │
 ▼
Tasklet
 │
 ▼
execute()
 │
 ▼
Perform Operation
 │
 ▼
Return RepeatStatus
```

The tasklet determines whether the step should finish or execute again based on the returned `RepeatStatus`.

---

## The Tasklet Interface

Spring Batch provides the `Tasklet` interface for implementing tasklet-based processing.

A tasklet implements:

```java
public interface Tasklet {

    RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception;
}
```

The `execute()` method contains the operation that the tasklet performs.

It receives two important arguments:

### `StepContribution`

`StepContribution` provides information about the current step execution and allows the tasklet to contribute to the step's execution information.

It provides access to the associated `StepExecution`:

```java
contribution.getStepExecution();
```

The `StepExecution` contains information about the current execution of a step, including its execution context and execution counters.

### `ChunkContext`

`ChunkContext` provides context associated with the current chunk or tasklet execution.

It can be used to access information associated with the current step execution and job execution.

Although tasklets receive a `ChunkContext`, they do not need to use it in every implementation.

---

## RepeatStatus

The tasklet's `execute()` method returns a `RepeatStatus`.

This value determines whether Spring Batch should execute the tasklet again.

The two commonly used values are:

### `FINISHED`

```java
return RepeatStatus.FINISHED;
```

Indicates that the tasklet has completed its work.

Spring Batch does not invoke the tasklet again for that step execution.

### `CONTINUABLE`

```java
return RepeatStatus.CONTINUABLE;
```

Indicates that the tasklet should be executed again.

This is useful when a tasklet needs multiple executions to complete its work.

For example, a tasklet might process a limited number of records during each invocation and continue until no more work remains.

Conceptually:

```text
Tasklet Execution
       │
       ▼
   execute()
       │
       ▼
  RepeatStatus
       │
       ├── FINISHED
       │      │
       │      ▼
       │   End Step
       │
       └── CONTINUABLE
              │
              ▼
        Execute Again
```

This example returns `RepeatStatus.FINISHED` because the cleanup operation is completed in one invocation.

---

## Tasklet Used in This Example

The example implements an `OrderCleanupTasklet`.

Its responsibility is to demonstrate a cleanup operation for a collection of expired order IDs.

The tasklet receives the order IDs through its constructor:

```java
public OrderCleanupTasklet(List<String> expiredOrderIds) {
    this.expiredOrderIds = List.copyOf(Objects.requireNonNull(expiredOrderIds, "expiredOrderIds must not be null"));
}
```

The constructor validates the supplied list and creates an immutable copy.

This prevents changes to the original list from affecting the tasklet after construction.

The tasklet then implements the `execute()` method:

```java
@Override
public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    
    expiredOrderIds.forEach(orderId -> log.info("Archiving expired order: " + orderId));

    ExecutionContext executionContext = contribution.getStepExecution().getExecutionContext();
    executionContext.putInt("orders.archived.count", expiredOrderIds.size());
    executionContext.putString("cleanup.status", "COMPLETED");

    return RepeatStatus.FINISHED;
}
```

The implementation demonstrates three things:

1. Performing the tasklet's operation.
2. Recording execution information in the step's execution context.
3. Returning `RepeatStatus.FINISHED` to indicate completion.

**Important:** The example uses an in-memory collection and logs the order IDs. It demonstrates the structure of a cleanup tasklet rather than performing actual database archiving.

---

## Understanding ExecutionContext

Spring Batch provides `ExecutionContext` for storing state associated with a job or step execution.

It behaves like a key-value store and supports storing execution information that can be accessed by other components during the relevant execution.

In this example, the tasklet stores two values:

```java
executionContext.putInt("orders.archived.count", expiredOrderIds.size());

executionContext.putString("cleanup.status", "COMPLETED");
```

The first value records the number of orders processed.

The second value records the cleanup operation's status.

The values can subsequently be retrieved:

```java
int archivedCount = executionContext.getInt("orders.archived.count");

String cleanupStatus = executionContext.getString("cleanup.status");
```

This makes it possible to inspect information produced during step execution.

### Step ExecutionContext vs Job ExecutionContext

Spring Batch provides execution contexts at different levels.

| Execution Context       | Scope                                        |
|-------------------------|----------------------------------------------|
| Step `ExecutionContext` | Associated with an individual step execution |
| Job `ExecutionContext`  | Associated with the overall job execution    |

This example uses the step's execution context:

```java
contribution.getStepExecution().getExecutionContext();
```

Therefore, the recorded cleanup information belongs to the execution of the cleanup step.

Execution contexts are also important when implementing stateful batch processing and restartable jobs.

---

## Configuring a Tasklet Step

Spring Batch provides `StepBuilder` for creating steps.

A tasklet can be registered with a step using the `tasklet()` method:

```java
@Bean
public Step cleanupOrdersStep(JobRepository jobRepository, OrderCleanupTasklet orderCleanupTasklet) {
    
    return new StepBuilder("cleanupOrdersStep", jobRepository).tasklet(orderCleanupTasklet).build();
}
```

The configuration demonstrates:

- Creating a step named `cleanupOrdersStep`.
- Supplying the `JobRepository`.
- Registering the tasklet.
- Building the step.

The `JobRepository` is responsible for storing batch metadata, including job and step execution information.

Unlike a chunk-oriented step, this step does not configure an item reader, processor, or writer.

The tasklet itself contains the operation to execute.

---

## Configuring the Job

The example registers the tasklet step with a Spring Batch job:

```java
@Bean
public Job taskletProcessingJob(JobRepository jobRepository, Step cleanupOrdersStep) {
    return new JobBuilder("taskletProcessingJob", jobRepository).start(cleanupOrdersStep).build();
}
```

The job contains one step:

```text
taskletProcessingJob
        │
        ▼
cleanupOrdersStep
        │
        ▼
OrderCleanupTasklet
        │
        ▼
Archive Expired Orders
        │
        ▼
FINISHED
```

When the job executes, Spring Batch starts the cleanup step, which invokes the registered tasklet.

---

## Tasklet Processing vs Chunk-Oriented Processing

Spring Batch provides two common approaches to implementing step logic:

- Tasklet processing.
- Chunk-oriented processing.

They solve different kinds of problems.

### Tasklet Processing

A tasklet performs an operation through its `execute()` method.

```text
Step
 │
 ▼
Tasklet
 │
 ▼
execute()
 │
 ▼
Perform Operation
 │
 ▼
FINISHED
```

This approach is suitable for operations that can be expressed as a single task or a controlled sequence of tasklet executions.

### Chunk-Oriented Processing

Chunk-oriented processing reads, processes, and writes items in groups.

```text
Reader
   │
   ▼
Processor
   │
   ▼
Writer
   │
   ▼
Commit Chunk
```

For example, a chunk size of three means that Spring Batch processes up to three items in a chunk before writing and committing that chunk.

### Comparison

| Feature          | Tasklet Processing                    | Chunk-Oriented Processing                        |
|------------------|---------------------------------------|--------------------------------------------------|
| Main abstraction | `Tasklet`                             | Reader, processor, writer                        |
| Execution        | Tasklet invocation                    | Repeated item processing                         |
| Processing model | Operation-oriented                    | Item-oriented                                    |
| Chunk size       | Not configured directly               | Configurable                                     |
| Typical use      | Cleanup, file operations, maintenance | Importing, transforming, exporting records       |
| Completion       | Controlled by `RepeatStatus`          | Controlled by item processing and step execution |

Neither approach is universally better. The appropriate choice depends on the nature of the operation.

---

## Tasklet Processing Flow

A simplified view of the complete example is:

```text
Spring Batch Job
       │
       ▼
taskletProcessingJob
       │
       ▼
cleanupOrdersStep
       │
       ▼
OrderCleanupTasklet
       │
       ▼
execute()
       │
       ├── Process expired order IDs
       │
       ├── Record archived order count
       │
       ├── Record cleanup status
       │
       ▼
RepeatStatus.FINISHED
       │
       ▼
Step Completed
       │
       ▼
Job Completed
```

---

## Important Considerations

### Tasklets Are Not Automatically Single-Invocation Operations

A tasklet is not necessarily executed only once.

Spring Batch uses the returned `RepeatStatus` to determine whether the tasklet should execute again.

Returning `FINISHED` ends the tasklet execution, while returning `CONTINUABLE` requests another invocation.

### Tasklet Processing Does Not Automatically Provide Business-Level Idempotency

A tasklet can be executed again when a job is restarted or when an execution is retried under an appropriate configuration.

The tasklet's business operation should therefore be designed carefully.

For example, a production cleanup tasklet should avoid archiving the same records repeatedly or producing inconsistent results after a failure.

### ExecutionContext Is Useful for Maintaining State

The execution context provides a way to record information associated with a job or step execution.

This becomes particularly useful when implementing restartable jobs, where execution state may need to be restored after an interruption.

### Transactions Depend on Step Configuration

Tasklet steps participate in Spring Batch's transaction management according to their configured transaction attributes and transaction manager.

For database operations, the transaction boundary is important because it determines how changes are committed or rolled back when an error occurs.

---

## Running the Example

From the repository root:

```bash
mvn -pl 07-spring-batch/tasklet-processing spring-boot:run
```

The application starts with the tasklet processing job configured.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 07-spring-batch/tasklet-processing test
```

To run the complete verification lifecycle:

```bash
mvn -pl 07-spring-batch/tasklet-processing clean verify
```

---

## Key Takeaways

- Tasklet processing is an operation-oriented programming model in Spring Batch.
- The `Tasklet` interface defines the `execute()` method.
- `StepContribution` provides access to the current step execution.
- `ChunkContext` provides context associated with tasklet execution.
- `RepeatStatus.FINISHED` indicates that the tasklet has completed.
- `RepeatStatus.CONTINUABLE` requests another tasklet invocation.
- `ExecutionContext` stores state associated with a job or step execution.
- Tasklet-based steps are configured using `StepBuilder.tasklet()`.
- Tasklets are suitable for operations that do not naturally fit into chunk-oriented processing.
- Unit tests verify tasklet behavior independently, while integration tests verify execution through the complete Spring Batch job.

---

## Next Example

The next example will explore **Job Execution** in Spring Batch, including job launching, job execution metadata, execution statuses, and monitoring the lifecycle of a batch job.
