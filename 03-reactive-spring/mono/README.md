# Mono

A small example demonstrating `Mono`, Reactor's publisher type for representing zero or one value.

This example builds on the Reactor fundamentals introduced in the previous example and focuses on creating, transforming, filtering, composing, and testing `Mono` publishers.

## Learning Objectives

By completing this example, you will understand:

- What `Mono` represents
- How to create a `Mono`
- How `Mono` represents a single value
- How `Mono` can complete without emitting a value
- How to transform values with `map`
- How to filter values
- How to provide fallback values
- How multiple `Mono` operators can be composed
- How lazy execution works with `Mono`
- How `Mono` represents errors
- How to test `Mono` with `StepVerifier`

---

## What Is Mono?

`Mono` is a Reactor publisher that can emit **zero or one value**.

Conceptually:

```text
Mono
 │
 ├── 0 values
 │
 └── 1 value
```

For example:

```java
Mono<String> name = Mono.just("John");
```

The `Mono` emits:

```text
John
```

and then completes.

A `Mono` can also complete without emitting a value:

```java
Mono<String> result = Mono.empty();
```

This makes `Mono` useful for operations that may return a single result or no result.

---

## Mono vs Flux

Reactor provides two commonly used publisher types:

| Type   | Values       |
|--------|--------------|
| `Mono` | Zero or one  |
| `Flux` | Zero or more |

For example:

```java
Mono<String> name = Mono.just("John");
```

represents at most one value.

A `Flux` can represent multiple values:

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");
```

The dedicated `Flux` example explores multiple-value reactive sequences in more detail.

---

## Creating a Mono

The simplest way to create a `Mono` containing a value is `Mono.just()`:

```java
Mono<String> name = Mono.just("John");
```

The value can then be consumed by subscribing:

```java
name.subscribe(System.out::println);
```

Output:

```text
John
```

---

## Empty Mono

A `Mono` does not have to emit a value.

Use `Mono.empty()` when the publisher should complete without a value:

```java
Mono<String> result = Mono.empty();
```

The resulting sequence is:

```text
onComplete
```

There is no `onNext` value.

This is useful when a reactive operation may have no result.

---

## Transforming Values

The `map` operator transforms the value emitted by a `Mono`.

For example:

```java
Mono<String> name = Mono.just("John");

Mono<String> uppercaseName = name.map(String::toUpperCase);
```

The resulting value is:

```text
JOHN
```

The original `Mono` is not modified. Instead, `map` creates another reactive pipeline.

Conceptually:

```text
"John"
   │
   ▼
  map
   │
   ▼
"JOHN"
```

---

## Filtering Values

`filter` allows a value to continue through the pipeline only when it satisfies a condition.

```java
Mono<String> name = Mono.just("John");

Mono<String> result = name.filter(value -> value.startsWith("J"));
```

Because `"John"` starts with `"J"`, the value is emitted.

If the condition does not match:

```java
Mono<String> result = Mono.just("John").filter(value -> value.startsWith("P"));
```

the `Mono` completes without emitting a value.

```text
Mono.just("John")
        │
        ▼
      filter
        │
        ▼
   condition false
        │
        ▼
    onComplete
```

---

## Handling Empty Results

A filtered `Mono` may complete without emitting a value.

A fallback can be provided with `defaultIfEmpty`:

```java
Mono<String> result = Mono.<String>empty().defaultIfEmpty("Unknown");
```

The resulting sequence emits:

```text
Unknown
```

This is useful when a reactive operation may not produce a result and a default value is appropriate.

More advanced error and fallback strategies are covered in the dedicated Reactive Error Handling example.

---

## Composing Mono Operators

Multiple operators can be chained together to form a reactive pipeline.

For example:

```java
Mono<String> result = Mono.just("john")
        .map(String::trim)
        .map(String::toUpperCase)
        .map(name -> "Hello " + name);
```

The data flows through the pipeline:

```text
"john"
   │
   ▼
 trim
   │
   ▼
"john"
   │
   ▼
 uppercase
   │
   ▼
"JOHN"
   │
   ▼
 add greeting
   │
   ▼
"Hello JOHN"
```

The resulting `Mono` still represents only one value.

---

## Lazy Execution

Like other Reactor publishers, `Mono` pipelines are lazy.

Consider:

```java
boolean[] executed = {false};

Mono<String> name = Mono.defer(() -> {
    executed[0] = true;
    
    return Mono.just("John");
});
```

Defining the `Mono` does not execute the supplier.

Execution happens when a subscriber subscribes:

```java
name.subscribe(System.out::println);
```

Conceptually:

```text
Define Mono
     │
     ▼
 No execution
     │
     │ subscribe()
     ▼
Execute supplier
     │
     ▼
 Emit "John"
     │
     ▼
 Complete
```

This allows reactive pipelines to describe work before that work is actually executed.

---

## Error Signals

A `Mono` can terminate with an error instead of completing successfully.

For example:

```java
Mono<String> result = Mono.error(new IllegalStateException("Something went wrong"));
```

The sequence terminates with an error:

```text
Mono
 │
 ▼
onError
```

An error is a terminal signal. Once the `Mono` terminates with an error, it does not emit another value or complete normally.

Detailed error recovery and fallback strategies are covered in the dedicated Reactive Error Handling example.

---

## Testing Mono

Reactor provides `StepVerifier` for testing reactive publishers.

For example:

```java
Mono<String> name = Mono.just("John");

StepVerifier.create(name).expectNext("John").verifyComplete();
```

`StepVerifier` can verify:

- emitted values
- completion
- empty sequences
- error signals
- the order of signals

For an empty `Mono`:

```java
Mono<String> result = Mono.empty();

StepVerifier.create(result).verifyComplete();
```

For an error:

```java
Mono<String> result = Mono.error(new IllegalStateException("Something went wrong"));

StepVerifier.create(result).expectError(IllegalStateException.class).verify();
```

---

## Complete Example

A simple `Mono` pipeline can combine the concepts introduced in this example:

```java
Mono<String> result = Mono.just("john")
        .map(String::trim)
        .filter(name -> !name.isEmpty())
        .map(String::toUpperCase)
        .defaultIfEmpty("UNKNOWN");
```

The pipeline can be represented as:

```text
Mono.just()
    │
    ▼
  trim
    │
    ▼
 filter
    │
    ▼
 uppercase
    │
    ▼
defaultIfEmpty
    │
    ▼
  Mono<String>
```

---

## Key Concepts

### `Mono`

A Reactor publisher that represents zero or one value.

### `Mono.just()`

Creates a `Mono` that emits a value and then completes.

### `Mono.empty()`

Creates a `Mono` that completes without emitting a value.

### `Mono.defer()`

Creates a `Mono` whose supplier is invoked when a subscriber subscribes.

### `map`

Transforms an emitted value.

### `filter`

Allows a value through only when a condition matches.

### `defaultIfEmpty`

Provides a fallback value when the source completes without emitting a value.

### `StepVerifier`

Provides a way to verify the signals emitted by a reactive publisher.

---

## Dependencies

The example uses Project Reactor directly.

```xml
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
</dependency>

<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-test</artifactId>
    <scope>test</scope>
</dependency>
```

JUnit and AssertJ are also used for testing.

---

## Running the Example

Run the tests from the example directory:

```bash
mvn clean test
```

You can also run `MonoApplication` directly from your IDE.

---

## Key Takeaways

1. `Mono` represents zero or one value.
2. `Mono.just()` creates a publisher containing one value.
3. `Mono.empty()` represents a publisher that completes without a value.
4. Operators such as `map` and `filter` can be composed into reactive pipelines.
5. `defaultIfEmpty` can provide a fallback for empty sequences.
6. `Mono` pipelines are lazy.
7. A `Mono` can terminate successfully or with an error.
8. `StepVerifier` can verify values and terminal signals.
9. `Mono` is commonly used for operations that produce at most one result.

The central idea is:

```text
        Mono
         │
    ┌────┴────┐
    │         │
  Value      Empty
    │         │
    ▼         ▼
 onNext    onComplete
    │
    ▼
onComplete
```

---

## Next

**Flux**

The next example explores `Flux`, Reactor's publisher type for representing zero or more values and working with reactive sequences.
