# Item Processors

This example demonstrates **item processing** in Spring Batch.

It focuses on how an `ItemProcessor` transforms individual items between reading and writing, how processed items can be filtered, and how Spring Batch handles the difference between items read, processed, filtered, and written.

The example uses a customer-processing job that reads customer names, normalizes them, filters out blank entries, and writes the remaining names.

## What This Example Demonstrates

- What item processing means in Spring Batch.
- The role of `ItemProcessor` in a batch job.
- How an item processor transforms input items.
- How to normalize data before writing it.
- How returning `null` filters an item from further processing.
- The relationship between `ItemReader`, `ItemProcessor`, and `ItemWriter`.
- How Spring Batch tracks read, processed, filtered, and written items.
- How to test an `ItemProcessor` independently.
- How to test item processing as part of a complete Spring Batch job.

---

## What Is Item Processing?

Item processing is the stage in a Spring Batch job where individual items are transformed, validated, or filtered before they are written.

A typical chunk-oriented batch step consists of three main components:

1. **ItemReader** — reads input items.
2. **ItemProcessor** — processes each item.
3. **ItemWriter** — writes the processed items.

The processing flow looks like:

```text
ItemReader
    │
    ▼
Read Item
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
Write Processed Items
```

The processor is optional. A step can read and write items without transforming them, but an `ItemProcessor` is useful when data needs to be prepared before it is written.

---

## The ItemProcessor Interface

Spring Batch provides the `ItemProcessor` interface for processing individual items.

Conceptually, its contract is:

```java
public interface ItemProcessor<I, O> {

    O process(I item) throws Exception;
}
```

It uses two generic types:

- `I` represents the input item type.
- `O` represents the output item type.

This allows the processor to transform an item from one type into another.

For example:

```java
ItemProcessor<String, String>
```

accepts a `String` and returns a `String`.

A processor can also produce a different type:

```java
ItemProcessor<CustomerInput, Customer>
```

This could transform raw customer input into a domain object.

The processor's `process` method is invoked for each item read by the `ItemReader`.

---

## Item Processing in This Example

This example processes customer names.

The input data contains:

```java
List.of(" Alice ", "Bob", "   ", "Charlie", " Diana ")
```

The processor performs two operations:

1. Removes leading and trailing whitespace.
2. Converts the customer name to uppercase.

Blank entries are filtered out.

The expected processing result is:

| Input       | Processed Result |
|-------------|------------------|
| `" Alice "` | `"ALICE"`        |
| `"Bob"`     | `"BOB"`          |
| `"   "`     | Filtered out     |
| `"Charlie"` | `"CHARLIE"`      |
| `" Diana "` | `"DIANA"`        |

The final output contains:

```text
ALICE
BOB
CHARLIE
DIANA
```

This demonstrates how a processor can both transform valid items and filter unwanted items.

---

## Implementing an ItemProcessor

The example defines `CustomerItemProcessor`, which implements Spring Batch's `ItemProcessor<String, String>`.

```java
public class CustomerItemProcessor implements ItemProcessor<String, String> {

    @Override
    public String process(String customerName) {
        String normalizedName = customerName.trim();

        if (normalizedName.isEmpty()) {
            return null;
        }

        return normalizedName.toUpperCase(Locale.ROOT);
    }
}
```

The implementation performs the following operations:

### 1. Normalize the Input

```java
String normalizedName = customerName.trim();
```

The `trim()` method removes leading and trailing whitespace.

For example:

```text
" Alice " → "Alice"
```

This ensures that unnecessary whitespace is removed before the item is written.

### 2. Filter Blank Items

```java
if (normalizedName.isEmpty()) {
    return null;
}
```

Spring Batch interprets a `null` result from an `ItemProcessor` as a filtered item.

The item is not passed to the `ItemWriter`.

For example:

```text
"   " → ""
     → null
```

The blank customer name is therefore excluded from the output.

### 3. Transform the Valid Item

```java
return normalizedName.toUpperCase(Locale.ROOT);
```

Valid customer names are converted to uppercase.

For example:

```text
"Alice" → "ALICE"
```

`Locale.ROOT` provides locale-independent case conversion, which is appropriate for this normalization example.

---

## Filtering Items

One of the important capabilities of an `ItemProcessor` is filtering.

A processor filters an item by returning `null`.

For example:

```java
@Override
public String process(String customerName) {
    if (customerName.isBlank()) {
        return null;
    }

    return customerName.trim().toUpperCase(Locale.ROOT);
}
```

When the processor returns `null`:

- The item has still been read.
- The processor has still been invoked.
- The item is not passed to the writer.
- Spring Batch records the item as filtered.

This is different from throwing an exception.

Filtering is an expected processing outcome when an item should be excluded from the output.

---

## Filtering vs Skipping

Filtering and skipping are different concepts in Spring Batch.

### Filtering

Filtering occurs when the processor intentionally returns `null`.

```text
ItemReader
    │
    ▼
ItemProcessor
    │
    ▼
Returns null
    │
    ▼
Item Filtered
    │
    ▼
Not Written
```

The item is excluded without treating the operation as an error.

### Skipping

Skipping occurs when an item causes an exception and the step is configured to tolerate that exception.

For example, a processor might throw an exception because an input value is invalid.

If the step has an appropriate skip policy, Spring Batch can skip the failed item and continue processing.

```text
ItemReader
    │
    ▼
ItemProcessor
    │
    ▼
Exception
    │
    ▼
Skip Policy
    │
    ▼
Item Skipped
```

The distinction is important:

| Behavior                    | Filtering                | Skipping                                                 |
|-----------------------------|--------------------------|----------------------------------------------------------|
| How it happens              | Processor returns `null` | Processing or another step operation throws an exception |
| Indicates an error          | No                       | Yes                                                      |
| Passed to writer            | No                       | No                                                       |
| Spring Batch metric         | Filter count             | Skip count                                               |
| Requires skip configuration | No                       | Yes                                                      |

This example demonstrates filtering. Skip and retry behavior is covered separately.

---

## ItemProcessor and Chunk Processing

The example uses a chunk-oriented step.

In chunk-oriented processing, Spring Batch reads items, processes them, and writes the resulting items in chunks.

The configuration uses a chunk size of two:

```java
private static final int CHUNK_SIZE = 2;
```

The step is configured with a reader, processor, and writer:

```java
new ChunkOrientedStepBuilder<String, String>("processCustomersStep",jobRepository,CHUNK_SIZE)
    .reader(customerItemReader)
    .processor(customerItemProcessor)
    .writer(customerItemWriter)
    .build();
```

The generic types indicate that the step reads `String` items and produces `String` items.

The processing flow is:

```text
Read Customer Names
        │
        ▼
CustomerItemReader
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
Write Processed Names
```

The processor is invoked for each item before the writer receives the resulting chunk.

Filtered items are removed from the chunk before it reaches the writer.

---

## Understanding the Batch Metrics

Spring Batch maintains execution statistics that help explain what happened during a step.

This example processes five input items.

The expected results are:

| Metric             | Expected Value | Meaning                                 |
|--------------------|---------------:|-----------------------------------------|
| Read count         |              5 | Five items were read                    |
| Write count        |              4 | Four processed items were written       |
| Filter count       |              1 | One item was filtered                   |
| Process skip count |              0 | No processing exceptions were skipped   |
| Commit count       |              3 | Three chunk transactions were committed |

### Read Count

The read count represents the number of items successfully read by the `ItemReader`.

In this example:

```text
Read Count = 5
```

### Filter Count

The filter count represents the number of items for which the processor returned `null`.

In this example:

```text
Filter Count = 1
```

The blank customer name is filtered out.

### Write Count

The write count represents the number of items successfully written.

In this example:

```text
Write Count = 4
```

Only the four normalized customer names reach the writer.

### Process Skip Count

The process skip count represents the number of items skipped because of exceptions during processing under the configured skip policy.

This example does not configure skip behavior and does not throw processing exceptions.

Therefore:

```text
Process Skip Count = 0
```

### Commit Count

The commit count represents the number of successful chunk transactions.

With a chunk size of two and five items read, the step processes three chunks:

```text
Chunk 1: Alice, Bob
Chunk 2: Blank, Charlie
Chunk 3: Diana
```

The blank item is filtered, but it was still read as part of the second chunk.

The resulting output is:

```text
Chunk 1: ALICE, BOB
Chunk 2: CHARLIE
Chunk 3: DIANA
```

Therefore:

```text
Commit Count = 3
```

---

## Registering the Processor as a Spring Bean

The example registers the processor through the job configuration:

```java
@Bean
public CustomerItemProcessor customerItemProcessor() {
    return new CustomerItemProcessor();
}
```

This makes the processor available for injection into the step configuration.

The reader and writer are also configured as beans.

The step therefore receives all three components:

```text
CustomerItemReader
        │
        ▼
CustomerItemProcessor
        │
        ▼
CustomerItemWriter
```

This keeps the responsibilities separate and makes each component easier to understand and test.

---

## Why Keep the Processor Separate?

The processor contains the business logic for transforming customer names.

Keeping this logic outside the job configuration provides several benefits:

- Processing logic is independent of job configuration.
- The processor can be tested without starting a Spring application.
- The same processing logic can be reused by another step if needed.
- Job configuration remains focused on assembling batch components.
- Changes to normalization rules do not require restructuring the job.

This follows the example project's preference for small, focused components.

---

## Running the Example

From the repository root:

```bash
mvn -pl 07-spring-batch/item-processors spring-boot:run
```

The application starts and executes the configured customer-processing job.

The job reads customer names, normalizes valid entries, filters blank entries, and writes the processed results.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 07-spring-batch/item-processors test
```

To perform a complete verification from the repository root:

```bash
mvn -pl 07-spring-batch/item-processors clean verify
```

---

## Key Takeaways

- An `ItemProcessor` processes individual items between reading and writing.
- The `ItemProcessor<I, O>` interface supports transformations between different input and output types.
- A processor can normalize, validate, enrich, or transform data.
- Returning `null` from a processor filters an item.
- Filtering is different from skipping: filtering is intentional, while skipping handles configured exceptions.
- Filtered items are counted as read but are not passed to the writer.
- Spring Batch tracks read count, write count, filter count, skip count, and commit count.
- Separating processing logic from job configuration makes it easier to test and maintain.
- Unit tests verify processor behavior, while integration tests verify its participation in the complete batch job.

---

## Next Example

The next example will explore **Item Writers** in Spring Batch, including how processed items are written to an output destination and how different writer implementations participate in chunk-oriented processing.
