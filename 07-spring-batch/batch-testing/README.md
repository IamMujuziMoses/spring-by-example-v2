# Batch Testing

This example demonstrates **testing Spring Batch jobs, steps, and individual batch components**.

It focuses on how to test batch components independently, launch complete jobs and individual steps in tests, verify execution results, and inspect the metadata recorded by Spring Batch.

The example uses an in-memory collection of orders, an H2 database for Spring Batch execution metadata, and Spring Batch's testing support.

## What This Example Demonstrates

- Why testing is important in Spring Batch applications.
- Unit testing item readers, processors, and writers.
- Testing complete batch jobs.
- Testing individual steps independently.
- Using `@SpringBatchTest`.
- Launching jobs with `JobOperatorTestUtils`.
- Supplying custom job parameters during tests.
- Testing step-scoped components.
- Using `MetaDataInstanceFactory` to create test execution metadata.
- Verifying job and step execution statuses.
- Inspecting read, write, and commit counts.
- Verifying persisted execution metadata through `JobRepository`.

---

## What Is Batch Testing?

Batch testing is the process of verifying that batch jobs and their individual components behave as expected.

A Spring Batch application can contain several components, including:

- Jobs.
- Steps.
- Item readers.
- Item processors.
- Item writers.
- Tasklets.
- Job parameters.
- Execution contexts.
- Job and step execution metadata.

Each component has a specific responsibility and can be tested independently or as part of a complete batch execution.

For example, an item processor can be tested without starting the Spring application context, while a complete job can be tested by launching it through Spring Batch's testing infrastructure.

A simplified testing structure looks like:

```text
Spring Batch Application
          |
          v
     Batch Testing
          |
          +----------------------+
          |                      |
          v                      v
    Unit Testing           Integration Testing
          |                      |
          v                      v
   Individual Components   Complete Jobs and Steps
          |                      |
          v                      v
   Reader Tests             Job Execution
   Processor Tests          Step Execution
   Writer Tests             Execution Metadata
```

This separation makes it easier to identify failures and verify individual responsibilities.

---

## Unit Testing Batch Components

Spring Batch components can be tested independently without starting the entire Spring application context.

This is particularly useful for readers, processors, and writers because their responsibilities can often be verified using ordinary unit tests.

This example includes unit tests for all three components.

### Item Reader Testing

The `OrderItemReader` reads orders from an in-memory collection.

Its responsibilities include:

- Returning orders in the expected order.
- Returning `null` when all orders have been consumed.
- Rejecting invalid constructor arguments.

A simplified test looks like:

```java
@Test
void read_shouldReturnOrdersInInputOrder() {
    Order first = new Order(1L, "first order");
    Order second = new Order(2L, "second order");

    OrderItemReader reader = new OrderItemReader(List.of(first, second));

    assertThat(reader.read()).isEqualTo(first);
    assertThat(reader.read()).isEqualTo(second);
}
```

The test verifies that the reader returns the expected items in their original order.

### Item Processor Testing

The `OrderItemProcessor` transforms an order into a processed order.

It converts the description to uppercase while preserving the original order identifier.

For example:

```text
Input:
Order(1, "first order")

Output:
ProcessedOrder(1, "FIRST ORDER")
```

A processor test looks like:

```java
@Test
void process_shouldTransformOrderDescriptionToUppercase() {
    Order order = new Order(1L, "first order");

    ProcessedOrder result = processor.process(order);

    assertThat(result).isEqualTo(new ProcessedOrder(1L, "FIRST ORDER"));
}
```

This test verifies the transformation independently of the batch job.

### Item Writer Testing

The `OrderItemWriter` receives processed orders in chunks and records them through `BatchTestingSupport`.

A writer test looks like:

```java
@Test
void write_shouldRecordProcessedOrders() {
    Chunk<ProcessedOrder> chunk = new Chunk<>(List.of(
        new ProcessedOrder(1L, "FIRST ORDER"),
        new ProcessedOrder(2L, "SECOND ORDER")
    ));

    writer.write(chunk);

    assertThat(support.writtenOrders()).hasSize(2);
}
```

This verifies that the writer correctly records the items it receives.

---

## Spring Batch Testing Support

Spring Batch provides testing utilities that simplify testing jobs, steps, and scoped components.

The example uses the `spring-batch-test` dependency.

```xml
<dependency>
    <groupId>org.springframework.batch</groupId>
    <artifactId>spring-batch-test</artifactId>
    <scope>test</scope>
</dependency>
```

The dependency provides utilities and test infrastructure for working with Spring Batch executions.

### `@SpringBatchTest`

The example uses:

```java
@SpringBatchTest
```

This annotation registers Spring Batch testing infrastructure, including support for:

- Job execution testing.
- Step execution testing.
- Job-scoped components.
- Step-scoped components.
- Test execution listeners.

It makes it easier to test batch components that depend on Spring Batch execution contexts.

### `JobOperatorTestUtils`

`JobOperatorTestUtils` provides utilities for launching jobs and individual steps during tests.

The example uses it to:

- Launch a complete job.
- Launch an individual step.
- Supply custom job parameters.

This allows tests to exercise actual Spring Batch execution rather than manually invoking individual components.

---

## Testing a Complete Job

The example defines a job named `batchTestingJob`.

It consists of two sequential steps:

1. `processOrdersStep`
2. `summarizeOrdersStep`

The first step processes orders, while the second records a summary of the successfully written orders.

The execution flow looks like:

```text
batchTestingJob
       |
       v
processOrdersStep
       |
       +---- Read Orders
       |
       +---- Process Orders
       |
       +---- Write Orders
       |
       v
summarizeOrdersStep
       |
       +---- Calculate Processed Count
       |
       +---- Store Summary
       |
       v
Job Completed
```

The integration test launches the complete job:

```java
JobExecution execution = jobOperatorTestUtils.startJob();
```

It then verifies the execution status:

```java
assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
```

The test also verifies that all five orders were processed and recorded.

This demonstrates how to test a complete job without manually invoking its individual steps.

---

## Testing an Individual Step

Sometimes it is useful to test one step without executing the entire job.

For example, a step may contain complex processing logic that needs to be verified independently of other steps.

`JobOperatorTestUtils` provides `startStep()` for this purpose.

The example uses:

```java
JobExecution execution = jobOperatorTestUtils.startStep("processOrdersStep");
```

This launches only the specified step.

The test verifies that:

- The execution completes successfully.
- Only one step execution is created.
- The executed step is `processOrdersStep`.
- All five orders are written.
- The summary step is not executed.

This is particularly useful when testing steps that are part of larger jobs.

---

## Testing Step-Scoped Components

Spring Batch supports scoped components whose lifecycle is tied to a particular job or step execution.

For example:

```java
@Bean
@StepScope
public OrderItemReader orderItemReader(BatchTestingSupport support) {
    return new OrderItemReader(support.orders());
}
```

The `@StepScope` annotation delays the creation of the reader until a step execution is available.

This allows a component to access step-related context and job parameters when necessary.

However, it also means that a step-scoped component cannot always be accessed directly from an ordinary unit test.

A step scope must be established before the component is accessed.

### `StepScopeTestExecutionListener`

`@SpringBatchTest` registers `StepScopeTestExecutionListener`.

The listener establishes the step scope for a test and makes the associated `StepExecution` available to step-scoped components.

It can discover a factory method named `getStepExecution()` in the test class.

The example provides:

```java
public StepExecution getStepExecution() {
    return MetaDataInstanceFactory.createStepExecution();
}
```

The returned execution supplies the context required by the step-scoped component.

The test can then access the reader:

```java
@Test
void read_shouldReadOrdersWithinStepScope() {
    Order order = reader.read();

    assertThat(order).isEqualTo(new Order(1L, "first order"));
}
```

This demonstrates how to test a step-scoped component without launching the complete batch job.

---

## `MetaDataInstanceFactory`

Spring Batch provides `MetaDataInstanceFactory` to create test instances of execution metadata objects.

For example:

```java
MetaDataInstanceFactory.createStepExecution();
```

This creates a `StepExecution` suitable for testing.

It is useful when a test requires execution metadata but does not need to launch an actual job through the `JobRepository`.

The factory can create objects such as:

- `JobInstance`.
- `JobExecution`.
- `StepExecution`.

These objects make it possible to test components that depend on Spring Batch execution context.

In this example, it is used to provide a `StepExecution` for the step-scoped reader test.

---

## Testing Job Execution Metadata

Spring Batch records metadata about job and step executions.

This metadata includes:

- Job name.
- Job instance.
- Execution status.
- Start and end times.
- Read count.
- Write count.
- Commit count.
- Skip counts.
- Step executions.

The example uses a JDBC-backed `JobRepository` to persist this metadata in an H2 in-memory database.

A test can inspect the step execution:

```java
StepExecution processingStep = execution.getStepExecutions()
    .stream()
    .filter(step -> step.getStepName().equals("processOrdersStep"))
    .findFirst()
    .orElseThrow();
```

It can then verify the execution counts:

```java
assertThat(processingStep.getReadCount()).isEqualTo(5);
assertThat(processingStep.getWriteCount()).isEqualTo(5);
assertThat(processingStep.getCommitCount()).isEqualTo(3);
```

The example uses a chunk size of `2` and processes five orders.

Therefore, the expected chunk commits are:

```text
First Chunk:  2 Orders
Second Chunk: 2 Orders
Third Chunk:  1 Order

Total Commits: 3
```

This demonstrates how execution metadata can be used to verify that a step processed its input as expected.

---

## Testing Persisted Execution Metadata

The `JobRepository` stores execution metadata so that it can be retrieved after a job has completed.

The example retrieves a completed execution using its identifier:

```java
JobExecution persistedExecution = jobRepository.getJobExecution(execution.getId());
```

The test verifies that the execution exists and has the expected status:

```java
assertThat(persistedExecution).isNotNull();

assertThat(persistedExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
```

This demonstrates that job execution metadata is persisted independently of the test's original execution reference.

---

## Testing Job Parameters

Job parameters influence how Spring Batch identifies and executes job instances.

The example demonstrates supplying custom parameters when launching a job.

For example:

```java
JobParameters parameters = new JobParametersBuilder().addString("test.run.id", UUID.randomUUID().toString())
    .toJobParameters();
```

The job is launched using:

```java
JobExecution execution = jobOperatorTestUtils.startJob(parameters);
```

The test verifies that the supplied parameter is available in the resulting execution:

```java
assertThat(execution.getJobParameters().getString("test.run.id")).isEqualTo(parameters.getString("test.run.id"));
```

This is useful when testing jobs that depend on runtime parameters, such as:

- Input file locations.
- Processing dates.
- Customer identifiers.
- Import identifiers.
- Execution-specific values.

Using unique identifying parameters also helps prevent job instance conflicts between test executions.

---

## Unit Testing vs Integration Testing

Spring Batch components can be tested at different levels.

### Unit Testing

Unit tests focus on individual components without starting the complete Spring application context.

Examples include:

- Testing item readers.
- Testing item processors.
- Testing item writers.

These tests are generally fast and isolate the behavior of individual components.

### Integration Testing

Integration tests verify that multiple Spring Batch components work together correctly.

Examples include:

- Launching a complete job.
- Launching an individual step.
- Verifying execution statuses.
- Inspecting execution metadata.
- Testing step-scoped components.

The distinction can be summarized as:

| Testing Type     | Focus                      | Example                         |
|------------------|----------------------------|---------------------------------|
| Unit testing     | Individual components      | Testing an item processor       |
| Step testing     | Individual steps           | Launching `processOrdersStep`   |
| Job testing      | Complete job execution     | Launching `batchTestingJob`     |
| Scope testing    | Scoped component lifecycle | Testing a step-scoped reader    |
| Metadata testing | Execution information      | Verifying read and write counts |

Both approaches are important when building reliable batch applications.

---

## Testing Flow

A simplified view of the example's testing approach is:

```text
                 Batch Testing
                       |
          +------------+------------+
          |                         |
          v                         v
     Unit Testing             Integration Testing
          |                         |
    +-----+------+            +-----+------+
    |     |      |            |            |
    v     v      v            v            v
 Reader Processor Writer    Job Tests   Step Tests
                                |            |
                                +------+-----+
                                       |
                                       v
                              Execution Metadata
                                       |
                                       v
                                Assertions
```

---

## Running the Example

From the repository root:

```bash
mvn -pl 07-spring-batch/batch-testing spring-boot:run
```

The application starts with the configured Spring Batch infrastructure.

The example is primarily intended to demonstrate batch testing through its automated test suite.

---

## Running the Tests

Run the tests from the module:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 07-spring-batch/batch-testing clean verify
```

---

## Test Coverage

The example contains the following tests:

| Test Class                       | Number of Tests | Purpose                                                 |
|----------------------------------|----------------:|---------------------------------------------------------|
| `OrderItemReaderTest`            |               3 | Reader behavior and exhaustion                          |
| `OrderItemProcessorTest`         |               3 | Item transformation and validation                      |
| `OrderItemWriterTest`            |               3 | Chunk writing and accumulation                          |
| `BatchTestingJobIntegrationTest` |               5 | Job execution, step execution, metadata, and parameters |
| `StepScopedOrderItemReaderTest`  |               1 | Step-scoped component testing                           |
| **Total**                        |          **15** |                                                         |

### What Is Verified?

The tests verify:

- Readers return items in the expected order.
- Readers return `null` when input is exhausted.
- Processors transform input correctly.
- Writers record processed items.
- Complete jobs execute successfully.
- Individual steps can be launched independently.
- Job and step execution statuses are correct.
- Read, write, and commit counts match expectations.
- Execution metadata can be retrieved from the repository.
- Custom job parameters are available during execution.
- Step-scoped components can be accessed within an established step scope.

---

## Key Takeaways

- Spring Batch provides testing utilities for jobs, steps, and scoped components.
- Individual batch components can be tested independently using unit tests.
- `@SpringBatchTest` registers Spring Batch testing infrastructure.
- `JobOperatorTestUtils` simplifies launching jobs and individual steps in tests.
- `MetaDataInstanceFactory` creates execution metadata objects for testing.
- Step-scoped components require an active step scope.
- `StepScopeTestExecutionListener` establishes the step scope for tests.
- Job and step execution metadata can be inspected to verify processing results.
- A JDBC-backed `JobRepository` allows execution metadata to be persisted and retrieved.
- Unit and integration tests complement each other when testing batch applications.

---

## Next Steps

This example completes the planned **Spring Batch** examples in Module 07.

