# Chunk Processing

This example demonstrates **chunk-oriented processing** in Spring Batch.

It focuses on how Spring Batch reads, processes, and writes data in chunks, how chunk size determines the number of items processed during each transaction, and how Spring Batch commits completed chunks.

The example uses an in-memory collection of order identifiers to demonstrate the complete chunk-processing workflow.

## What This Example Demonstrates

- What chunk-oriented processing means in Spring Batch.
- The relationship between `ItemReader`, `ItemProcessor`, and `ItemWriter`.
- How `ChunkOrientedStepBuilder` configures a chunk-oriented step.
- How chunk size determines the number of items processed together.
- How Spring Batch reads items individually.
- How an `ItemProcessor` transforms individual items.
- How an `ItemWriter` receives and writes a collection of processed items.
- How Spring Batch commits chunks.
- How a final chunk can contain fewer items than the configured chunk size.
- Testing chunk boundaries and execution statistics.

---

## What Is Chunk-Oriented Processing?

Chunk-oriented processing is a processing model in Spring Batch where data is read one item at a time, processed individually, and written in groups called chunks.

Instead of processing an entire dataset in one operation, Spring Batch divides the workload into smaller groups.

A chunk-oriented step follows this general sequence:

```text
Read Items
    │
    ▼
Process Items
    │
    ▼
Collect Items into a Chunk
    │
    ▼
Write Chunk
    │
    ▼
Commit Transaction
    │
    ▼
Next Chunk
```

This approach is useful when processing large datasets because it avoids loading and processing the entire dataset in memory as a single operation.

It also provides transaction boundaries around groups of items.

---

## Chunk Processing Components

A chunk-oriented step typically consists of three main components:

- `ItemReader`
- `ItemProcessor`
- `ItemWriter`

Each component has a specific responsibility.

```text
             ItemReader
                 │
                 ▼
           ItemProcessor
                 │
                 ▼
             ItemWriter
                 │
                 ▼
          Transaction Commit
```

### ItemReader

The `ItemReader` retrieves individual items from a data source.

In this example, the reader retrieves order identifiers from an in-memory list.

```java
public class OrderItemReader implements ItemReader<String> {

    private final Iterator<String> orderIds;

    public OrderItemReader(List<String> orderIds) {
        this.orderIds = List.copyOf(Objects.requireNonNull(orderIds, "orderIds must not be null")).iterator();
    }

    @Override
    public String read() {
        return orderIds.hasNext() ? orderIds.next() : null;
    }
}
```

The reader returns one order identifier per invocation.

For example:

```text
order-101
order-102
order-103
order-104
order-105
order-106
order-107
```

When there are no more items to read, it returns `null`.

In Spring Batch, returning `null` indicates that the input has been exhausted.

### ItemProcessor

The `ItemProcessor` transforms or validates each item before it is written.

This example converts order identifiers to uppercase.

```java
public class OrderItemProcessor implements ItemProcessor<String, String> {

    @Override
    public String process(String orderId) {
        return orderId.toUpperCase(Locale.ROOT);
    }
}
```

For example:

```text
order-101 → ORDER-101
order-102 → ORDER-102
order-103 → ORDER-103
```

The processor receives one item at a time, not an entire chunk.

A processor can also return `null` to indicate that an item should be filtered out and excluded from the output.

This example transforms every item and does not filter any records.

### ItemWriter

The `ItemWriter` receives a chunk of processed items and writes them to the destination.

Unlike the reader and processor, which operate on individual items, the writer receives multiple items together.

This example uses an in-memory writer to record the chunks it receives.

```java
public class ChunkRecordingItemWriter implements ItemWriter<String> {

    private final List<List<String>> writtenChunks = new ArrayList<>();

    @Override
    public void write(Chunk<? extends String> chunk) {
        writtenChunks.add(List.copyOf(chunk.getItems()));
    }

    public List<List<String>> getWrittenChunks() {
        return List.copyOf(writtenChunks);
    }
}
```

The writer records each chunk separately so that the example can demonstrate how Spring Batch groups processed items.

The recorded chunks can then be inspected in tests.

---

## Configuring Chunk Processing

The example uses `ChunkOrientedStepBuilder` to configure a chunk-oriented step.

The configuration defines a chunk size of three:

```java
private static final int CHUNK_SIZE = 3;
```

The step is configured as follows:

```java
@Bean
public Step processOrdersStep(JobRepository jobRepository, ItemReader<String> orderItemReader,
        ItemProcessor<String, String> orderItemProcessor, ItemWriter<String> chunkRecordingItemWriter) {

    return new ChunkOrientedStepBuilder<String, String>("processOrdersStep", jobRepository, CHUNK_SIZE)
            .reader(orderItemReader)
            .processor(orderItemProcessor)
            .writer(chunkRecordingItemWriter)
            .build();
}
```

The builder receives:

- The step name.
- The `JobRepository`.
- The configured chunk size.

The reader, processor, and writer are then supplied to the step.

The chunk size determines how many processed items Spring Batch collects before invoking the writer, except when the input is exhausted and the final chunk contains fewer items.

---

## Understanding Chunk Size

Chunk size determines the number of items processed and written together before a transaction is committed.

This example uses:

```java
private static final int CHUNK_SIZE = 3;
```

There are seven order identifiers to process:

```java
private static final List<String> ORDER_IDS = List.of("order-101", "order-102", "order-103", "order-104", "order-105",
        "order-106", "order-107");
```

With a chunk size of three, Spring Batch divides the input into three chunks.

### First Chunk

```text
ORDER-101
ORDER-102
ORDER-103
```

Three items are read, processed, and passed to the writer.

After successful writing, the transaction is committed.

### Second Chunk

```text
ORDER-104
ORDER-105
ORDER-106
```

The next three items are processed and written together.

After successful writing, another transaction is committed.

### Third Chunk

```text
ORDER-107
```

Only one item remains.

Spring Batch writes this final partial chunk and commits the transaction.

The final chunk does not need to contain the full configured chunk size.

---

## Visualizing Chunk Boundaries

The complete processing sequence looks like this:

```text
Input Items
    │
    ▼
┌──────────────────────────┐
│ order-101                │
│ order-102                │
│ order-103                │
└──────────────────────────┘
    │
    ▼
Process Items
    │
    ▼
┌──────────────────────────┐
│ ORDER-101                │
│ ORDER-102                │
│ ORDER-103                │
└──────────────────────────┘
    │
    ▼
Write Chunk 1
    │
    ▼
Commit Transaction 1
    │
    ▼
┌──────────────────────────┐
│ order-104                │
│ order-105                │
│ order-106                │
└──────────────────────────┘
    │
    ▼
Process Items
    │
    ▼
┌──────────────────────────┐
│ ORDER-104                │
│ ORDER-105                │
│ ORDER-106                │
└──────────────────────────┘
    │
    ▼
Write Chunk 2
    │
    ▼
Commit Transaction 2
    │
    ▼
┌──────────────────────────┐
│ order-107                │
└──────────────────────────┘
    │
    ▼
Process Item
    │
    ▼
┌──────────────────────────┐
│ ORDER-107                │
└──────────────────────────┘
    │
    ▼
Write Chunk 3
    │
    ▼
Commit Transaction 3
```

The result is three successful chunk writes and three transaction commits.

---

## Chunk Size and Transaction Boundaries

Chunk-oriented processing is closely related to transaction management.

In a typical chunk-oriented step, Spring Batch processes a chunk within a transaction.

The general lifecycle is:

```text
Begin Transaction
       │
       ▼
Read Items
       │
       ▼
Process Items
       │
       ▼
Write Chunk
       │
       ▼
Commit Transaction
```

If the chunk is written successfully, the transaction can be committed.

If an error occurs during processing or writing, the transaction may be rolled back according to the configured transaction manager and processing behavior.

This means that chunk size also influences transaction boundaries.

For example:

| Chunk Size | Input Items | Number of Chunks |
|------------|------------:|-----------------:|
| 1          |           7 |                7 |
| 2          |           7 |                4 |
| 3          |           7 |                3 |
| 4          |           7 |                2 |
| 7          |           7 |                1 |

A smaller chunk size generally means more frequent commits, while a larger chunk size means fewer commits with more items processed in each transaction.

The appropriate chunk size depends on the workload, transaction requirements, resource constraints, and performance characteristics of the application.

---

## Understanding the Final Partial Chunk

One important detail of chunk-oriented processing is that the final chunk does not have to be full.

In this example:

```text
Configured Chunk Size: 3
Total Items:           7
```

The resulting chunks are:

```text
Chunk 1: 3 items
Chunk 2: 3 items
Chunk 3: 1 item
```

Spring Batch writes the remaining items when the reader indicates that there are no more items.

It does not discard the final items simply because the configured chunk size has not been reached.

The number of chunks can be calculated as:

```text
Number of Chunks = Ceiling(Total Items / Chunk Size)
```

For this example:

```text
Ceiling(7 / 3) = 3
```

Therefore, three chunks are expected.

---

## Configuring the Job

The example defines a job named `chunkProcessingJob`.

```java
@Bean
public Job chunkProcessingJob(JobRepository jobRepository, Step processOrdersStep) {

    return new JobBuilder("chunkProcessingJob", jobRepository).start(processOrdersStep).build();
}
```

The job contains one chunk-oriented step:

```text
chunkProcessingJob
        │
        ▼
processOrdersStep
        │
        ▼
ItemReader
        │
        ▼
ItemProcessor
        │
        ▼
ItemWriter
```

The job is responsible for coordinating the execution of the step.

The step performs the actual chunk-oriented processing.

---

## Job Parameters

The integration test launches the job with a unique identifying parameter:

```java
JobParameters jobParameters = new JobParametersBuilder()
        .addJobParameter("run.id", UUID.randomUUID().toString(), String.class, true)
        .toJobParameters();
```

The unique parameter allows the same job to be launched multiple times during testing.

Spring Batch uses identifying job parameters when determining job instance identity.

Without a unique identifying parameter, attempting to launch an already completed job instance again may result in an exception because that job instance has already completed.

---

## Why Use an In-Memory Writer?

This example uses `ChunkRecordingItemWriter` instead of writing to a database or file.

The purpose is to make chunk boundaries visible and easy to verify.

The writer records each chunk as a separate list:

```text
[
    [ORDER-101, ORDER-102, ORDER-103],
    [ORDER-104, ORDER-105, ORDER-106],
    [ORDER-107]
]
```

This makes it possible to verify that Spring Batch passes the expected groups of items to the writer.

The in-memory writer is an educational implementation, not a production persistence solution.

In real applications, an `ItemWriter` might write to:

- A relational database.
- A CSV file.
- A message queue.
- An external API.
- Another persistent storage system.

---

## Chunk Processing vs Item Processing

Item processing and chunk processing are related but distinct concepts.

### Item Processing

An `ItemProcessor` handles one item at a time.

```text
order-101 → ORDER-101
order-102 → ORDER-102
order-103 → ORDER-103
```

### Chunk Processing

Chunk processing coordinates the reading, processing, and writing of multiple items within a chunk.

```text
Read 3 Items
     │
     ▼
Process 3 Items
     │
     ▼
Write 3 Items
     │
     ▼
Commit Transaction
```

The processor transforms individual items, while the chunk-oriented step manages the overall processing and transaction lifecycle.

---

## Important Design Considerations

### Choosing a Chunk Size

Chunk size should be selected based on the application's workload.

A very small chunk size may result in frequent transaction commits and additional overhead.

A very large chunk size may increase memory consumption and the amount of work involved in a transaction rollback.

The right value depends on the application's data volume, transaction characteristics, and performance requirements.

### Transaction Management

Chunk-oriented processing normally uses transaction boundaries around chunks.

The transaction manager determines how the actual resources participate in those transactions.

For example, database writes can participate in a transaction managed by the configured transaction manager.

### Reader and Writer Responsibilities

The reader retrieves input data, while the writer persists or forwards processed data.

Keeping these responsibilities separate makes batch jobs easier to understand, test, and maintain.

### Processing Large Datasets

Chunk-oriented processing is particularly useful when handling large datasets because it processes data incrementally rather than requiring the entire dataset to be loaded into memory at once.

---

## Running the Example

From the repository root:

```bash
mvn -pl 07-spring-batch/chunk-processing spring-boot:run
```

The application starts and executes the configured Spring Batch job.

The job reads seven order identifiers, converts them to uppercase, and writes them in chunks of three.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 07-spring-batch/chunk-processing test
```

To run the complete verification lifecycle, including compilation, tests, and configured quality checks:

```bash
mvn -pl 07-spring-batch/chunk-processing clean verify
```

---

## Key Takeaways

- Chunk-oriented processing divides batch workloads into smaller groups of items.
- An `ItemReader` retrieves individual items from a data source.
- An `ItemProcessor` transforms or validates individual items.
- An `ItemWriter` receives processed items grouped into a chunk.
- `ChunkOrientedStepBuilder` configures a chunk-oriented step.
- Chunk size determines how many processed items are written together before a transaction commits.
- The final chunk can contain fewer items than the configured chunk size.
- Successful chunk processing results in a transaction commit for each completed chunk.
- Chunk-oriented processing is useful for handling large datasets incrementally.
- Separating readers, processors, and writers makes batch jobs easier to test and maintain.

---

## Next Example

The next example will explore **Tasklets** in Spring Batch, demonstrating how tasklet-oriented steps execute a single unit of work rather than processing data through a reader, processor, and writer.
