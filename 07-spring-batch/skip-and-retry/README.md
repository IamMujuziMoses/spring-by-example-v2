# Skip and Retry

This example demonstrates **skip and retry fault-tolerance mechanisms** in Spring Batch.

It focuses on how Spring Batch handles exceptions during chunk-oriented processing, how skip policies determine which items can be skipped, and how retry policies allow transient failures to be retried before a step fails.

The example uses an in-memory collection of customer orders to demonstrate successful processing, skipped items, retry attempts, and exhausted skip and retry limits.

## What This Example Demonstrates

- What fault tolerance means in Spring Batch.
- Chunk-oriented processing with fault tolerance.
- Handling invalid items using skip policies.
- Handling transient failures using retry policies.
- Configuring skip limits.
- Configuring retry limits.
- The difference between skipped items and retried items.
- How Spring Batch records skip and write counts.
- How skip and retry limits affect step and job execution.
- Testing fault-tolerant batch processing.

---

## What Is Fault Tolerance?

Fault tolerance allows a batch job to handle certain processing failures without immediately terminating the entire job.

In real-world batch applications, individual records may contain invalid data, or processing operations may fail temporarily because of external dependencies.

For example:

- An order may contain invalid information.
- A record may reference a customer that does not exist.
- An external service may temporarily be unavailable.
- A database operation may fail because of a temporary connection problem.

Without fault tolerance, an exception during item processing can cause the step to fail.

Spring Batch provides skip and retry mechanisms to handle these situations.

A simplified flow looks like:

```text
Batch Job
    |
    v
Read Item
    |
    v
Process Item
    |
    +---- Success ----------------> Write Item
    |
    +---- Exception
              |
              v
       Fault Tolerance
              |
              +---- Skip
              |
              +---- Retry
              |
              +---- Fail
```

---

## Skip Processing

Skipping allows a batch job to continue processing when an item encounters a configured exception.

This is useful when an individual record is invalid but should not prevent other valid records from being processed.

For example, consider a batch containing these orders:

```text
Order 1 ---- Valid
Order 2 ---- Invalid
Order 3 ---- Valid
Order 4 ---- Valid
```

If invalid orders are configured to be skipped, the batch can continue processing the remaining orders.

The result is:

```text
Order 1 ---- Processed
Order 2 ---- Skipped
Order 3 ---- Processed
Order 4 ---- Processed
```

The job can complete successfully even though an item was rejected.

---

## Configuring a Skip Policy

Spring Batch supports skip policies to determine whether an exception should result in an item being skipped.

This example uses `LimitCheckingExceptionHierarchySkipPolicy`:

```java
new LimitCheckingExceptionHierarchySkipPolicy(Set.of(InvalidOrderException.class),2)
```

The configuration specifies:

- `InvalidOrderException` is eligible for skipping.
- The maximum skip limit is `2`.

The policy considers the exception hierarchy when checking whether an exception is skippable.

This means that an exception matching the configured type, including an appropriate subclass, can be skipped.

### Skip Limit

The skip limit controls how many skippable exceptions a step can tolerate.

For example:

```text
Skip Limit: 2

First invalid order  ---- Skipped
Second invalid order ---- Skipped
Third invalid order  ---- Step fails
```

An important detail is that when the skip limit is exceeded, the exception that exceeds the limit is not counted as another successful skip.

Therefore, with a skip limit of `2`, the step's skip count remains `2` when the third skippable exception causes the step to fail.

---

## Retry Processing

Retry allows Spring Batch to attempt processing an item again when a configured exception occurs.

This is useful for transient failures that may disappear when an operation is repeated.

For example, an external service might temporarily fail while processing an order.

Instead of immediately failing the step, Spring Batch can retry the operation.

Conceptually:

```text
Process Order
     |
     v
Transient Failure
     |
     v
   Retry
     |
     v
Process Order Again
     |
     +---- Success ----> Continue
     |
     +---- Failure ----> Retry Again
```

If the operation eventually succeeds within the configured retry limit, processing continues normally.

---

## Configuring a Retry Policy

This example uses Spring Framework's `RetryPolicy`:

```java
RetryPolicy.builder().maxRetries(2).includes(TransientOrderProcessingException.class).build();
```

The configuration specifies:

- `TransientOrderProcessingException` is eligible for retry.
- A maximum of `2` retries is allowed.

The retry count does not include the initial processing attempt.

Therefore:

```text
Initial Attempt
      |
      v
First Retry
      |
      v
Second Retry
      |
      v
Success or Failure
```

A maximum of two retries means an item can be processed up to three times in total: one initial attempt and two retries.

If all attempts fail, the step fails.

---

## Skip vs Retry

Skip and retry solve different problems.

### Skip

Skip is appropriate when an item cannot be processed successfully and should be excluded from the current batch execution.

Example:

```text
Invalid Order
     |
     v
InvalidOrderException
     |
     v
Skip Item
     |
     v
Continue Processing
```

### Retry

Retry is appropriate when an operation may succeed if attempted again.

Example:

```text
Order Processing
      |
      v
Temporary Failure
      |
      v
Retry Processing
      |
      v
Successful Processing
```

The distinction is important:

| Mechanism | Purpose                                              | Example                      |
|-----------|------------------------------------------------------|------------------------------|
| Skip      | Exclude an item after a skippable failure            | Invalid order                |
| Retry     | Attempt an operation again after a retryable failure | Temporary processing failure |
| Fail      | Stop the step when an error cannot be handled        | Retry or skip limit exceeded |

---

## Fault-Tolerant Chunk Processing

This example uses chunk-oriented processing with a chunk size of `3`.

The step reads, processes, and writes items in chunks.

A simplified flow looks like:

```text
Read Items
    |
    v
Process Items
    |
    +---- Invalid Item ----> Skip
    |
    +---- Transient Error -> Retry
    |
    v
Write Successful Items
    |
    v
Commit Chunk
```

The step is configured using `ChunkOrientedStepBuilder`:

```java
new ChunkOrientedStepBuilder<CustomerOrder, ProcessedOrder>("processOrdersStep", jobRepository, 3)
    .reader(orderItemReader)
    .processor(orderItemProcessor)
    .writer(orderItemWriter)
    .faultTolerant()
    .skipPolicy(skipPolicy)
    .retryPolicy(retryPolicy)
    .build();
```

The builder configures the reader, processor, writer, and fault-tolerance policies.

The skip and retry policies determine how the step responds to their respective exceptions.

---

## Example Scenarios

The example provides three scenarios to demonstrate different outcomes.

### 1. Successful Processing with Skip and Retry

The `success` scenario contains four orders:

| Order | Description | Behavior                      |
|-------|-------------|-------------------------------|
| 1     | Order 1     | Processed successfully        |
| 2     | Order 2     | Skipped because it is invalid |
| 3     | Order 3     | Fails twice, then succeeds    |
| 4     | Order 4     | Processed successfully        |

Expected result:

```text
Job Status: COMPLETED

Orders Read: 4
Process Skip Count: 1
Orders Written: 3
Order 3 Processing Attempts: 3
```

The invalid order is skipped, while the transient failure is successfully recovered through retries.

The remaining valid orders are written.

### 2. Retry Limit Exceeded

The `retry-exhausted` scenario contains an order that fails processing three times.

With a maximum of two retries, the execution looks like:

```text
Initial Attempt ---- Failure
       |
       v
First Retry -------- Failure
       |
       v
Second Retry ------- Failure
       |
       v
Step Failed
```

Expected result:

```text
Job Status: FAILED

Order Processing Attempts: 3
Orders Written: 0
```

The job fails because the processing exception remains unresolved after the permitted retries.

### 3. Skip Limit Exceeded

The `skip-exhausted` scenario contains three invalid orders.

The configured skip limit is `2`.

Execution:

```text
Order 1 ---- Invalid ---- Skipped
Order 2 ---- Invalid ---- Skipped
Order 3 ---- Invalid ---- Skip Limit Exceeded
                              |
                              v
                          Step Failed
```

Expected result:

```text
Job Status: FAILED

Process Skip Count: 2
Orders Written: 0
```

The third invalid order exceeds the configured skip limit, causing the step to fail.

---

## Item Processor

The `OrderItemProcessor` contains the processing logic.

It performs two important checks:

1. Determines whether an order is invalid.
2. Simulates transient processing failures.

An invalid order causes an `InvalidOrderException`:

```java
if (order.invalid()) {
    throw new InvalidOrderException("Order is invalid: " + order.id());
}
```

A transient failure causes a `TransientOrderProcessingException` until the configured number of failures has been reached:

```java
int attempt = support.recordProcessingAttempt(order.id());

if (attempt <= order.transientFailuresBeforeSuccess()) {
    throw new TransientOrderProcessingException("Temporary failure processing order: " + order.id());
}
```

Once processing succeeds, the processor returns a `ProcessedOrder`:

```java
return new ProcessedOrder(order.id(), order.description().toUpperCase(Locale.ROOT));
```

This keeps the example focused on the distinction between permanent validation failures and recoverable processing failures.

---

## Custom Exceptions

The example defines two exception types.

### `InvalidOrderException`

Represents an order that cannot be processed because its data is invalid.

It is configured as a skippable exception.

### `TransientOrderProcessingException`

Represents a temporary processing failure.

It is configured as a retryable exception.

Separating these exceptions makes the fault-tolerance configuration explicit and easier to understand.

---

## Job Parameters

The example uses a `scenario` job parameter to select which set of orders should be processed.

For example:

```text
scenario=success
scenario=retry-exhausted
scenario=skip-exhausted
```

The reader uses the scenario parameter to obtain the corresponding input data.

A unique `run.id` parameter is also supplied to create a distinct job instance for each test execution.

This allows the tests to execute the same scenario independently without conflicting with previous job instances.

---

## JobRepository and Execution Metadata

The application uses a JDBC-backed `JobRepository`.

The repository stores Spring Batch execution metadata, including:

- Job instances.
- Job executions.
- Step executions.
- Execution statuses.
- Read, write, and skip counts.

The example uses an H2 in-memory database for the repository.

This allows the integration tests to verify execution metadata without requiring an external database.

---

## Running the Example

From the repository root:

```bash
mvn -pl 07-spring-batch/skip-and-retry spring-boot:run
```

The application starts with the configured Spring Batch infrastructure.

The example is primarily intended to demonstrate batch execution through its integration tests.

---

## Running the Tests

Run the tests from the module:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 07-spring-batch/skip-and-retry clean verify
```

---

## Key Takeaways

- Fault tolerance allows batch jobs to handle selected processing failures.
- Skip policies determine which exceptions can be skipped.
- Skip limits control how many skippable exceptions a step can tolerate.
- Retry policies determine which exceptions should trigger another processing attempt.
- Retry limits control the number of additional attempts.
- A retry is not the same as a skip.
- Transient failures are good candidates for retry.
- Invalid records may be candidates for skipping.
- Exceeding a skip or retry limit can cause a step and job to fail.
- Spring Batch exposes execution counts that help monitor fault-tolerant processing.
- Skip and retry policies should be chosen according to the application's data integrity and recovery requirements.

---

## What This Example Does Not Cover

This example intentionally focuses on basic skip and retry behavior in chunk-oriented processing.

It does not cover:

- Custom skip listeners.
- Retry listeners.
- Backoff policies.
- Stateful retry.
- Retry across transaction boundaries.
- Database-backed business data persistence.
- Dead-letter queues.
- External service recovery strategies.
- Complex exception classification.
- Distributed batch processing.

These topics can be explored separately when needed.

---

## Next Example

The next example will explore **Batch Testing** in Spring Batch, including testing jobs and steps, launching jobs in tests, verifying execution metadata, and testing batch components independently.
