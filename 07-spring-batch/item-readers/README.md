# Item Readers

This example demonstrates **item reading in Spring Batch**.

It focuses on how Spring Batch reads individual records from a data source, how an `ItemReader` provides records to a batch step, and how chunk-oriented processing consumes the records returned by the reader.

The example implements a custom `ItemReader` that reads customer names sequentially from an in-memory collection and supplies them to a chunk-oriented step.

## What This Example Demonstrates

- What item reading means in Spring Batch.
- The purpose and responsibility of `ItemReader`.
- The `ItemReader<T>` interface.
- Implementing a custom item reader.
- Reading records sequentially.
- Signalling the end of input by returning `null`.
- Registering an item reader as a Spring bean.
- Using `@StepScope` for step-scoped components.
- Connecting an item reader to a chunk-oriented step.
- How chunk size affects item processing.
- Testing a custom item reader independently.
- Testing item reading through a complete Spring Batch job.

---

## What Is an Item Reader?

An item reader is a component responsible for reading individual records from an input source during batch processing.

Spring Batch defines the `ItemReader<T>` interface for this purpose.

The reader provides one item at a time to the batch processing infrastructure.

Conceptually:

```text
Input Source
     │
     ▼
ItemReader
     │
     ▼
Item
     │
     ▼
Batch Processing
```

An input source can be many different things, including:

- A database.
- A CSV file.
- An XML file.
- A JSON file.
- A flat file.
- An in-memory collection.
- A remote service.

Spring Batch provides reader implementations for common input sources, while also allowing developers to create custom readers for application-specific requirements.

---

## The ItemReader Interface

The `ItemReader<T>` interface defines a simple contract:

```java
public interface ItemReader<T> {

    T read() throws Exception;

}
```

The `read()` method returns the next item from the input source.

There are two important outcomes:

| Return value | Meaning                                   |
|--------------|-------------------------------------------|
| An item      | Another item is available for processing. |
| `null`       | No more items are available.              |

Returning `null` signals that the input has been exhausted.

For example:

```java
@Override
public String read() {
    if (customers.hasNext()) {
        return customers.next();
    }

    return null;
}
```

The reader does not need to know what happens to an item after it has been returned. Its responsibility is to supply the next item.

---

## Custom Item Reader

This example implements a custom reader named `CustomerItemReader`.

The reader receives a collection of customer names and returns them sequentially.

```java
public class CustomerItemReader implements ItemReader<String> {

    private final Iterator<String> customers;

    public CustomerItemReader(List<String> customerNames) {
        Objects.requireNonNull(customerNames, "Customer names must not be null");
        this.customers = List.copyOf(customerNames).iterator();
    }

    @Override
    public String read() {
        if (customers.hasNext()) {
            return customers.next();
        }

        return null;
    }

}
```

### Understanding the Implementation

**1. Implementing `ItemReader<String>`**

```java
public class CustomerItemReader implements ItemReader<String>
```

The reader implements `ItemReader<String>`, meaning that each invocation of `read()` returns a customer name.

**2. Maintaining the iterator**

```java
private final Iterator<String> customers;
```

The iterator maintains the current position in the collection.

It allows the reader to retrieve the next customer without manually managing an index.

**3. Initializing the reader**

```java
public CustomerItemReader(List<String> customerNames) {
    Objects.requireNonNull(customerNames, "Customer names must not be null");
    this.customers = List.copyOf(customerNames).iterator();
}
```

The constructor receives the customer names and creates an iterator over an immutable copy of the supplied collection.

Using `List.copyOf()` prevents changes to the original collection from affecting the reader after initialization.

It also rejects null elements.

**4. Reading the next item**

```java
@Override
public String read() {
    if (customers.hasNext()) {
        return customers.next();
    }

    return null;
}
```

Each call returns the next available customer name.

Once the collection is exhausted, the method returns `null` to signal that there are no more items.

---

## Registering the Item Reader as a Spring Bean

The example registers the custom reader in `ItemReaderJobConfiguration`.

```java
@Bean
@StepScope
public ItemReader<String> customerItemReader() {
    return new CustomerItemReader(CUSTOMERS);
}
```

This makes the reader available to the batch step through Spring dependency injection.

### Why `@StepScope`?

`@StepScope` creates the bean within the lifecycle of a step execution.

This is useful for components that maintain state during processing.

An item reader commonly maintains state because it tracks which records have already been read.

In this example, the reader uses an iterator. A step-scoped reader ensures that a fresh reader instance is created for each step execution rather than sharing an exhausted iterator across executions.

Step scope also makes it possible to inject job and step execution context values into readers when required.

---

## Connecting the Reader to a Batch Step

An item reader is typically used as part of a chunk-oriented step.

This example uses a chunk-oriented step to read customer names and pass them to an item writer.

```java
@Bean
public Step readCustomersStep(JobRepository jobRepository, ItemReader<String> customerItemReader, 
        ItemWriter<String> customerItemWriter) {

    return new ChunkOrientedStepBuilder<String, String>("readCustomersStep", jobRepository, CHUNK_SIZE)
            .reader(customerItemReader)
            .writer(customerItemWriter)
            .build();
}
```

The step connects three important elements:

- `ItemReader`: Supplies items.
- `ItemProcessor`: Optionally transforms or filters items.
- `ItemWriter`: Receives and writes a collection of items.

This example intentionally does not include an item processor. The reader supplies customer names directly to the writer.

Conceptually:

```text
Customer Data
      │
      ▼
 ItemReader
      │
      ▼
 Chunk Processing
      │
      ▼
 ItemWriter
```

---

## Understanding Chunk Processing

Spring Batch supports chunk-oriented processing, where items are read and processed in groups called chunks.

Instead of processing and writing every item individually, the framework collects items until the configured chunk size is reached.

For example:

```java
private static final int CHUNK_SIZE = 2;
```

With a chunk size of `2`, the reader supplies items in groups of two.

This example uses five customer names:

```java
private static final List<String> CUSTOMERS = List.of("Alice", "Bob", "Charlie", "Diana", "Edward");
```

The resulting chunks are:

```text
Chunk 1
  Alice
  Bob
     │
     ▼
   Writer

Chunk 2
  Charlie
  Diana
     │
     ▼
   Writer

Chunk 3
  Edward
     │
     ▼
   Writer
```

The final chunk can contain fewer items than the configured chunk size.

For five items and a chunk size of two:

| Chunk | Items          | Item count |
|-------|----------------|-----------:|
| 1     | Alice, Bob     |          2 |
| 2     | Charlie, Diana |          2 |
| 3     | Edward         |          1 |

The step therefore processes three chunks.

---

## The Item Writer

The example uses a simple item writer to demonstrate that the reader's output reaches the writing stage.

```java
@Bean
public ItemWriter<String> customerItemWriter() {
    return chunk -> LOGGER.info("Writing customer names: {}", chunk.getItems());
}
```

The writer receives a chunk containing the items collected during the read phase.

It logs the customer names rather than persisting them to a database or another external destination.

This keeps the example focused on item reading while still demonstrating how a reader participates in a complete chunk-oriented step.

A production application would typically use an appropriate writer to persist or export the processed items.

---

## Item Reading Flow

The complete flow in this example is:

```text
Spring Batch Job
       │
       ▼
readCustomersStep
       │
       ▼
CustomerItemReader
       │
       ▼
Read Alice
       │
       ▼
Read Bob
       │
       ▼
Chunk of 2 Items
       │
       ▼
CustomerItemWriter
       │
       ▼
Read Charlie
       │
       ▼
Read Diana
       │
       ▼
Chunk of 2 Items
       │
       ▼
CustomerItemWriter
       │
       ▼
Read Edward
       │
       ▼
End of Input (null)
       │
       ▼
Final Chunk of 1 Item
       │
       ▼
CustomerItemWriter
       │
       ▼
Step Completed
       │
       ▼
Job Completed
```

The reader continues supplying items until it returns `null`.

Spring Batch then completes the final chunk and finishes the step.

---

## Testing Item Readers

The example contains two test classes:

- `CustomerItemReaderTest`
- `ItemReaderJobConfigurationTest`

They cover the reader's individual behavior and its integration with Spring Batch.

### 1. Testing Sequential Reading

The unit test verifies that the reader returns items in the expected order.

```java
CustomerItemReader reader = new CustomerItemReader(List.of("Alice", "Bob", "Charlie"));

assertThat(reader.read()).isEqualTo("Alice");
assertThat(reader.read()).isEqualTo("Bob");
assertThat(reader.read()).isEqualTo("Charlie");
```

This verifies that the reader maintains its position and supplies the next item on each invocation.

### 2. Testing End of Input

Once all available items have been read, the reader must return `null`.

```java
assertThat(reader.read()).isNull();
assertThat(reader.read()).isNull();
```

This verifies that the reader correctly signals the end of its input.

Repeated calls after exhaustion must not produce additional items.

### 3. Testing Empty Input

The reader must also handle an empty collection.

```java
CustomerItemReader reader = new CustomerItemReader(List.of());

assertThat(reader.read()).isNull();
```

An empty input collection contains no items, so the first read should immediately return `null`.

### 4. Testing Null Input

The constructor rejects a null collection.

```java
assertThatNullPointerException()
        .isThrownBy(() -> new CustomerItemReader(null))
        .withMessage("Customer names must not be null");
```

This verifies that invalid input is rejected during reader initialization rather than causing an unexpected failure later during execution.

### 5. Testing the Batch Job

The integration test launches the configured job using `JobOperator`.

The test supplies a unique identifying `run.id` parameter so that each test execution can create a distinct job instance.

```java
JobParameters jobParameters = new JobParametersBuilder()
        .addJobParameter("run.id", UUID.randomUUID().toString(), String.class, true)
        .toJobParameters();

JobExecution jobExecution = jobOperator.start(readCustomersJob, jobParameters);
```

The test verifies that:

- The job completes successfully.
- The expected step executes.
- The step completes successfully.
- Five items are read.
- Five items are written.
- Three chunks are committed.

For the configured chunk size of two, the expected execution metrics are:

| Metric       | Expected value |
|--------------|---------------:|
| Read count   |              5 |
| Write count  |              5 |
| Commit count |              3 |

These assertions verify that the reader participates correctly in the complete batch execution.

---

## Why Use a Unique Job Parameter in Tests?

Spring Batch uses job parameters to identify job instances.

A job instance is associated with a job and its identifying parameters.

If a test repeatedly launches the same job with the same identifying parameters, Spring Batch may recognize that it is attempting to execute an existing job instance.

Using a unique parameter avoids conflicts between repeated test executions:

```java
.addJobParameter("run.id", UUID.randomUUID().toString(), String.class, true)
```

The final `true` marks the parameter as identifying.

This makes each test execution use a distinct job instance.

---

## Common Item Reader Implementations

Spring Batch provides readers for different input sources.

Some commonly used implementations include:

| Reader                 | Purpose                                           |
|------------------------|---------------------------------------------------|
| `FlatFileItemReader`   | Reads records from flat files such as CSV files.  |
| `JdbcCursorItemReader` | Reads database records using a JDBC cursor.       |
| `JdbcPagingItemReader` | Reads database records using paging.              |
| `JpaPagingItemReader`  | Reads database records using JPA pagination.      |
| `JsonItemReader`       | Reads records from JSON input.                    |
| Custom `ItemReader`    | Implements application-specific reading behavior. |

The appropriate reader depends on the input source, data volume, resource constraints, and restartability requirements.

This example uses a custom reader to make the `ItemReader` contract easy to understand before introducing readers backed by external data sources.

---

## ItemReader vs ItemProcessor vs ItemWriter

These components have separate responsibilities in chunk-oriented processing.

| Component       | Responsibility                            |
|-----------------|-------------------------------------------|
| `ItemReader`    | Reads the next item from an input source. |
| `ItemProcessor` | Optionally transforms or filters an item. |
| `ItemWriter`    | Writes a chunk of processed items.        |

A typical batch processing flow looks like:

```text
Input Source
     │
     ▼
ItemReader
     │
     ▼
ItemProcessor
     │
     ▼
ItemWriter
     │
     ▼
Output Destination
```

An item processor is optional. A step can connect a reader directly to a writer when no transformation is necessary.

---

## Running the Example

From the repository root:

```bash
mvn -pl 07-spring-batch/item-readers spring-boot:run
```

The application starts with the Spring Batch infrastructure configured.

Automatic job execution can be controlled through the application's Spring Batch configuration.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 07-spring-batch/item-readers test
```

To run the complete module verification, including the configured quality checks:

```bash
mvn -pl 07-spring-batch/item-readers clean verify
```

---

## Key Takeaways

- An `ItemReader<T>` supplies individual items to a Spring Batch step.
- The `read()` method returns the next available item.
- Returning `null` signals that no more items are available.
- Custom readers can implement application-specific input requirements.
- `@StepScope` gives stateful readers a lifecycle associated with a step execution.
- Chunk-oriented processing groups items before passing them to the writer.
- The final chunk can contain fewer items than the configured chunk size.
- A reader can be tested independently of the Spring Batch infrastructure.
- Integration tests verify that the reader works correctly within a complete job.
- Spring Batch provides built-in readers for common input sources.
- Restartability requires additional consideration when a reader maintains state.

---

## Next Example

The next example will explore **Item Processors** in Spring Batch, including how to transform, validate, and filter items between the reading and writing stages.
