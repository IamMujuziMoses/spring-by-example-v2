# Item Writers

This example demonstrates **item writing** in Spring Batch.

It focuses on how Spring Batch writes processed items to an output destination, how an `ItemWriter` receives items in chunks, and how writing is integrated into chunk-oriented processing.

The example uses a customer-processing job that reads customer names, normalizes them, filters out blank entries, and writes the remaining names to an in-memory output destination using a custom `ItemWriter`.

## What This Example Demonstrates

- What item writing means in Spring Batch.
- The role of `ItemWriter` in a batch job.
- How an `ItemWriter` receives items through a `Chunk`.
- How to implement a custom `ItemWriter`.
- Writing multiple items in a single operation.
- Writing items to an output destination.
- The relationship between `ItemReader`, `ItemProcessor`, and `ItemWriter`.
- How chunk size influences writer invocations.
- How filtered items are excluded from writing.
- How to test an `ItemWriter` independently.
- How to test item writing as part of a complete Spring Batch job.

---

## What Is Item Writing?

Item writing is the stage in a Spring Batch job where processed items are delivered to an output destination.

A typical chunk-oriented batch step consists of three main components:

1. **ItemReader** — reads input items.
2. **ItemProcessor** — optionally transforms or filters items.
3. **ItemWriter** — writes the resulting items to an output destination.

The processing flow looks like:

```text
ItemReader
    │
    ▼
Read Items
    │
    ▼
ItemProcessor
    │
    ▼
Transform or Filter
    │
    ▼
ItemWriter
    │
    ▼
Output Destination
```

The `ItemWriter` is responsible for handling the items produced by the preceding processing stages.

Depending on the application, the destination might be:

- A relational database.
- A flat file.
- A message queue.
- An external service.
- An in-memory collection.

Spring Batch provides writer implementations for common destinations, while custom writers can be created when application-specific behavior is required.

---

## The ItemWriter Interface

Spring Batch provides the `ItemWriter` interface for writing items.

Conceptually, its contract is:

```java
public interface ItemWriter<T> {

    void write(Chunk<? extends T> chunk) throws Exception;
}
```

The generic type `T` represents the type of item the writer receives.

For example:

```java
ItemWriter<String>
```

accepts chunks containing strings.

Unlike an `ItemProcessor`, which processes one item at a time, an `ItemWriter` receives a collection of processed items through a `Chunk`.

This allows the writer to handle multiple items in one operation.

For example, with a chunk size of three:

```text
Chunk 1: [ALICE, BOB, CHARLIE]
Chunk 2: [DIANA, ERIC, FRANK]
```

The writer receives each chunk and writes its contents to the configured destination.

---

## Item Writing in This Example

This example writes normalized customer names.

The input data contains:

```java
List.of(" Alice ", "Bob", "   ", "Charlie", " Diana ")
```

The processor performs two operations:

1. Removes leading and trailing whitespace.
2. Converts customer names to uppercase.

Blank entries are filtered out.

The expected processed items are:

```text
ALICE
BOB
CHARLIE
DIANA
```

The writer receives these items in chunks and sends them to an in-memory output destination.

---

## Implementing an ItemWriter

The example defines `CustomerItemWriter`, which implements Spring Batch's `ItemWriter<String>`.

```java
public class CustomerItemWriter implements ItemWriter<String> {

    private final Consumer<List<String>> output;

    public CustomerItemWriter(Consumer<List<String>> output) {
        this.output = Objects.requireNonNull(output, "Output consumer must not be null");
    }

    @Override
    public void write(Chunk<? extends String> chunk) {
        output.accept(List.copyOf(chunk.getItems()));
    }
}
```

The implementation has three important parts.

### 1. Defining the Output Destination

```java
private final Consumer<List<String>> output;
```

The writer receives a `Consumer<List<String>>` that represents its output destination.

This keeps the writer independent of a particular storage mechanism.

The destination determines what happens to the items after the writer receives them.

For example, the destination could:

- Log the items.
- Add them to a collection.
- Send them to another component.
- Delegate to a persistence service.

In this example, the configured consumer logs the customer names.

### 2. Validating the Output Destination

```java
this.output = Objects.requireNonNull(output,"Output consumer must not be null");
```

The constructor rejects a null output destination.

This ensures that the writer always has a valid destination when it is created.

### 3. Writing the Chunk

```java
@Override
public void write(Chunk<? extends String> chunk) {
    output.accept(List.copyOf(chunk.getItems()));
}
```

The writer receives a chunk and passes its items to the configured output destination.

The `chunk.getItems()` method provides the items contained in the chunk.

`List.copyOf()` creates an immutable copy before passing the items to the destination.

This avoids exposing the chunk's mutable collection directly to the output consumer.

---

## Why Does ItemWriter Receive a Chunk?

Spring Batch uses chunk-oriented processing to group items into manageable units of work.

Instead of writing each item individually, the writer receives a group of processed items.

For example, with a chunk size of two:

```text
First Write
    │
    ▼
[ALICE, BOB]

Second Write
    │
    ▼
[CHARLIE]

Third Write
    │
    ▼
[DIANA]
```

This approach provides several benefits:

- Reduces the overhead of performing individual write operations.
- Allows writers to perform bulk operations.
- Supports transaction boundaries around chunk processing.
- Makes it possible to coordinate reading, processing, and writing as a unit.

The exact behavior of a writer depends on its implementation and the destination it interacts with.

---

## Understanding the Output Destination

The example uses an in-memory output destination configured through a `Consumer<List<String>>`.

The writer itself does not decide where customer names are stored.

Instead, the output destination is supplied when the writer is constructed.

The configuration uses:

```java
@Bean
public CustomerItemWriter customerItemWriter() {
    return new CustomerItemWriter(customerNames -> LOGGER.info("Writing customer names: {}", customerNames));
}
```

The consumer logs the contents of each chunk.

Expected output:

```text
Writing customer names: [ALICE, BOB]
Writing customer names: [CHARLIE]
Writing customer names: [DIANA]
```

This example intentionally uses logging as a lightweight output destination rather than introducing database or file-writing configuration.

The focus is on understanding the `ItemWriter` contract and how Spring Batch delivers chunks to a writer.

---

## ItemWriter and Chunk Processing

The example uses a chunk-oriented step with a chunk size of two.

```java
private static final int CHUNK_SIZE = 2;
```

The step integrates the reader, processor, and writer:

```java
new ChunkOrientedStepBuilder<String, String>("writeCustomersStep", jobRepository, CHUNK_SIZE)
    .reader(customerItemReader)
    .processor(customerItemProcessor)
    .writer(customerItemWriter)
    .build();
```

The generic types indicate that the step reads `String` items and produces `String` items.

The writer receives the processed output from the processor.

The complete flow is:

```text
CustomerItemReader
        │
        ▼
Read Customer Names
        │
        ▼
CustomerItemProcessor
        │
        ├── Normalize valid names
        │
        └── Filter blank names
        │
        ▼
CustomerItemWriter
        │
        ▼
Output Destination
```

---

## How Chunk Size Affects Writing

The chunk size determines how many input items are processed before the chunk is written.

This example uses:

```java
private static final int CHUNK_SIZE = 2;
```

There are five input items:

```text
1. " Alice "
2. "Bob"
3. "   "
4. "Charlie"
5. " Diana "
```

The reader supplies items in groups of two, followed by the remaining item.

The processing and writing flow is:

### First Chunk

Input:

```text
[" Alice ", "Bob"]
```

Processed output:

```text
["ALICE", "BOB"]
```

The writer receives:

```text
[ALICE, BOB]
```

### Second Chunk

Input:

```text
["   ", "Charlie"]
```

Processed output:

```text
["CHARLIE"]
```

The blank entry is filtered out.

The writer receives:

```text
[CHARLIE]
```

### Third Chunk

Input:

```text
[" Diana "]
```

Processed output:

```text
["DIANA"]
```

The writer receives:

```text
[DIANA]
```

The final writer output is therefore:

```text
[ALICE, BOB]
[CHARLIE]
[DIANA]
```

An important detail is that the chunk size applies to items read for processing, not necessarily the number of items ultimately passed to the writer.

Filtering can therefore result in a writer receiving fewer items than the configured chunk size.

---

## ItemWriter and Transactions

In chunk-oriented processing, Spring Batch coordinates writing with the step's transaction management.

Conceptually, a chunk is processed as a unit:

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

If an error occurs during chunk processing, transaction handling determines whether the current chunk is committed or rolled back.

The exact behavior depends on the transaction manager and the writer's destination.

For example, a database writer participating in the step transaction can write records within the same transaction managed by Spring Batch.

This example uses a logging consumer, so it does not demonstrate durable persistence or transactional rollback of an external destination.

Those concerns are important when implementing writers that interact with databases, files, or external systems.

---

## ItemWriter vs ItemProcessor

An `ItemProcessor` and an `ItemWriter` have different responsibilities.

### ItemProcessor

The processor transforms or filters individual items.

```java
@Override
public String process(String customerName) {
    String normalizedName = customerName.trim();

    if (normalizedName.isEmpty()) {
        return null;
    }

    return normalizedName.toUpperCase(Locale.ROOT);
}
```

### ItemWriter

The writer receives the processed items in a chunk and sends them to an output destination.

```java
@Override
public void write(Chunk<? extends String> chunk) {
    output.accept(List.copyOf(chunk.getItems()));
}
```

The distinction can be summarized as:

| Component       | Responsibility              | Input           |
|-----------------|-----------------------------|-----------------|
| `ItemReader`    | Reads items                 | Input source    |
| `ItemProcessor` | Transforms or filters items | Individual item |
| `ItemWriter`    | Writes processed items      | Chunk of items  |

Each component has a focused responsibility within the batch step.

---

## Why Keep the Writer Separate?

The writer contains the logic for delivering processed items to an output destination.

Keeping this logic separate from the job configuration provides several benefits:

- Writing behavior is independent of job configuration.
- The writer can be tested without launching a Spring application.
- The output destination can be changed without modifying the processing logic.
- The writer can be reused by another step when appropriate.
- Job configuration remains focused on assembling batch components.

The use of a `Consumer<List<String>>` also makes the output destination easy to replace for testing or future extensions.

---

## Understanding the Batch Metrics

Spring Batch tracks execution statistics for each step.

This example processes five input items and writes four processed items.

| Metric             | Expected Value | Meaning                                 |
|--------------------|---------------:|-----------------------------------------|
| Read count         |              5 | Five items were read                    |
| Write count        |              4 | Four items were written                 |
| Filter count       |              1 | One item was filtered                   |
| Process skip count |              0 | No processing exceptions were skipped   |
| Commit count       |              3 | Three chunk transactions were committed |

The difference between read count and write count is caused by the blank customer name filtered by the processor.

The writer receives only the four valid processed names.

---

## Running the Example

From the repository root:

```bash
mvn -pl 07-spring-batch/item-writers spring-boot:run
```

The application starts and executes the configured customer-writing job.

The job reads customer names, normalizes valid entries, filters blank entries, and writes the remaining names through the configured output destination.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 07-spring-batch/item-writers test
```

To perform a complete verification from the repository root:

```bash
mvn -pl 07-spring-batch/item-writers clean verify
```

---

## Key Takeaways

- An `ItemWriter` is responsible for writing processed items to an output destination.
- Spring Batch passes items to the writer through a `Chunk`.
- A writer can handle multiple items in a single write operation.
- Chunk size influences how many items are processed before a write operation.
- Filtering can result in a writer receiving fewer items than the configured chunk size.
- Spring Batch coordinates chunk writing with step transaction management.
- Custom writers can be implemented for application-specific output destinations.
- Separating the writer from job configuration improves testability and maintainability.
- Unit tests verify writer behavior, while integration tests verify its participation in a complete batch job.

---

## Next Example

The next example will explore **Chunk Processing** in Spring Batch, including how chunk size, transactions, reading, processing, and writing work together during batch execution.
