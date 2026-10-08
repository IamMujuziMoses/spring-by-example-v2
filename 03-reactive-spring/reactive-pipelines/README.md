# Reactive Pipelines

This example demonstrates how reactive pipelines are built by composing Reactor operators.

The goal is to understand how values flow through a sequence of transformations and how different Reactor operators can be combined to build a complete reactive pipeline.

## What You'll Learn

- How reactive pipelines are composed
- How operators transform and filter values
- How `map` transforms values
- How `filter` controls which values continue through a pipeline
- How `flatMap` works with nested publishers
- How `concatMap` preserves source ordering
- How `Mono` and `Flux` can be composed
- How `flatMapMany` expands a `Mono` into a `Flux`
- How `thenMany` continues with another publisher after completion
- How `switchIfEmpty` provides a fallback publisher
- How reactive pipelines are lazy
- How `StepVerifier` can verify complete pipelines

---

## Reactive Pipelines

A reactive pipeline is a sequence of operators applied to a reactive publisher.

Each operator receives the result of the previous stage and returns another publisher that can be used by the next stage.

For example:

```java
Flux<String> result = Flux.just("spring", "reactor", "webflux", "data")
        .filter(value -> value.startsWith("r") || value.startsWith("w"))
        .map(String::toUpperCase)
        .map(value -> "Topic: " + value);
```

The values flow through the pipeline:

```text
Flux.just(...)
      │
      ▼
   filter
      │
      ▼
     map
      │
      ▼
     map
      │
      ▼
  Subscriber
```

The result is:

```text
Topic: REACTOR
Topic: WEBFLUX
```

---

## Operator Composition

Reactive pipelines become useful when multiple operators are composed together.

```java
Flux<String> result = Flux.just("spring", "reactor", "webflux", "data")
        .filter(value -> value.startsWith("r") || value.startsWith("w"))
        .map(String::toUpperCase)
        .map(value -> "Topic: " + value);
```

Each operator has a specific responsibility:

1. `filter` selects the values that should continue.
2. The first `map` transforms the selected values.
3. The second `map` adds additional information to each value.

This allows complex processing to be expressed as a sequence of small operations.

---

## `map`

`map` transforms each value emitted by a publisher into another value.

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");

Flux<String> uppercaseNames = names.map(String::toUpperCase);
```

The number of values remains the same:

```text
John  →  JOHN
Jane  →  JANE
Peter →  PETER
```

`map` is appropriate when one value can be transformed directly into another value.

---

## `filter`

`filter` controls which values are allowed to continue through the pipeline.

```java
Flux<Integer> numbers = Flux.range(1, 6);

Flux<Integer> evenNumbers = numbers.filter(number -> number % 2 == 0);
```

The resulting sequence is:

```text
2
4
6
```

Values that do not satisfy the condition are discarded.

---

## `flatMap`

`flatMap` is useful when transforming a value requires another reactive publisher.

Unlike `map`, which transforms a value into another value, `flatMap` transforms a value into a `Publisher` and merges the resulting publishers.

```java
Flux<String> result = Flux.just("John", "Jane").flatMap(name -> Mono.just("Hello " + name));
```

Conceptually:

```text
John ──→ Mono("Hello John") ──┐
                              ├──→ Flux
Jane ──→ Mono("Hello Jane") ──┘
```

Because `flatMap` can subscribe to multiple inner publishers, the order of results should not generally be relied upon when the inner publishers execute asynchronously.

---

## `concatMap`

`concatMap` also transforms each value into another publisher, but subscribes to those publishers sequentially.

```java
Flux<String> result = Flux.just("John", "Jane", "Peter").concatMap(name -> Mono.just("Hello " + name));
```

The resulting sequence preserves the source order:

```text
Hello John
Hello Jane
Hello Peter
```

This makes `concatMap` useful when the order of the source sequence must be preserved.

---

## `flatMapMany`

A `Mono` can be expanded into a multi-value `Flux` using `flatMapMany`.

```java
Mono<List<String>> names = Mono.just(List.of("John", "Jane", "Peter"));
Flux<String> result = names.flatMapMany(Flux::fromIterable);
```

The transformation is:

```text
Mono<List<String>>
        │
        ▼
flatMapMany
        │
        ▼
Flux<String>
        │
        ├── John
        ├── Jane
        └── Peter
```

This is useful when a single reactive result determines a subsequent multi-value sequence.

---

## `thenMany`

`thenMany` waits for the source publisher to complete, ignores its emitted values, and then subscribes to another publisher.

```java
Flux<String> result = Flux.just("John", "Jane").thenMany(Flux.just("Complete"));
```

The values emitted by the first publisher are not passed downstream.

The resulting sequence is:

```text
Complete
```

This is useful when the completion of one reactive operation should trigger another reactive operation.

---

## `switchIfEmpty`

`switchIfEmpty` provides an alternative publisher when the original publisher completes without emitting a value.

```java
Flux<String> result = Flux.<String>empty().switchIfEmpty(Flux.just("No results"));
```

The resulting sequence is:

```text
No results
```

This is useful for reactive fallback scenarios.

---

## Mono and Flux Composition

`Mono` and `Flux` can participate in the same reactive pipeline.

For example, a `Mono` containing one value can be expanded into a `Flux` containing multiple values:

```java
Mono<String> category = Mono.just("Spring");

Flux<String> result = category.flatMapMany(value -> Flux.just(value + " Core", value + " Boot", value + " Data"));
```

The result is:

```text
Spring Core
Spring Boot
Spring Data
```

This demonstrates that reactive pipelines are not restricted to a single publisher type.

`Mono` and `Flux` can be combined depending on the shape of the data being processed.

---

## Lazy Execution

Reactive pipelines are lazy.

Creating and configuring a pipeline does not immediately execute the operations inside it.

Execution begins when a subscriber subscribes.

```java
boolean[] executed = {false};

Flux<String> result = Flux.defer(() -> {
    executed[0] = true;

    return Flux.just("Spring", "Reactor");
}).map(String::toUpperCase);

assertThat(executed[0]).isFalse();

result.subscribe();

assertThat(executed[0]).isTrue();
```

The important distinction is:

```text
Build Pipeline
      │
      ▼
No execution
      │
      ▼
Subscribe
      │
      ▼
Pipeline executes
```

This lazy execution model is an important part of reactive programming.

---

## Dependencies

The example uses Project Reactor and Reactor Test.

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

<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
```

Dependency versions are managed by the parent Spring by Example V2 project.

---

## Running the Example

From the module directory:

```bash
mvn test
```

Or from the project root:

```bash
mvn -pl 03-reactive-spring/reactive-pipelines test
```

To run the complete Maven build:

```bash
mvn clean install
```

---

## Key Takeaways

- A reactive pipeline is built by composing operators.
- Each operator returns another publisher that can participate in the next stage.
- `map` transforms values directly.
- `filter` controls which values continue through the pipeline.
- `flatMap` transforms values into publishers and merges their results.
- `concatMap` processes inner publishers sequentially and preserves source order.
- `flatMapMany` expands a `Mono` into a `Flux`.
- `thenMany` continues with another publisher after completion.
- `switchIfEmpty` provides a fallback publisher when no values are emitted.
- `Mono` and `Flux` can be composed together.
- Reactive pipelines are lazy and execute when subscribed.
- `StepVerifier` provides a convenient way to test reactive sequences.

---

## What's Next?

The next example focuses on **Backpressure** and explores how reactive streams handle situations where a producer can emit data faster than a consumer can process it.

> Small examples. Extensive coverage.
