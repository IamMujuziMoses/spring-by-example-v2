# Flux

A small example demonstrating `Flux`, Reactor's publisher type for representing zero or more values.

This example builds on the Reactor and `Mono` fundamentals introduced in the previous examples and focuses on creating, transforming, filtering, limiting, and testing multi-value reactive sequences.

## Learning Objectives

By completing this example, you will understand:

- What `Flux` represents
- How to create a `Flux`
- How `Flux` can emit multiple values
- How to create a `Flux` from a range of values
- How to create a `Flux` from an existing collection
- How to transform values with `map`
- How to filter values with `filter`
- How to limit values with `take`
- How an empty `Flux` behaves
- How to collect emitted values into a `List`
- How lazy execution works with `Flux`
- How to test reactive sequences with `StepVerifier`

---

## What Is Flux?

`Flux` is a Reactor publisher that can emit **zero or more values**.

Conceptually:

```text
Flux
 │
 ├── 0 values
 ├── 1 value
 ├── 2 values
 ├── 3 values
 └── ...more values
```

For example:

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");
```

The `Flux` emits:

```text
John
Jane
Peter
```

and then completes.

---

## Flux vs Mono

Reactor provides two commonly used publisher types:

| Type   | Values       |
|--------|--------------|
| `Mono` | Zero or one  |
| `Flux` | Zero or more |

A `Mono` is useful when an operation can produce at most one result:

```java
Mono<String> name = Mono.just("John");
```

A `Flux` is useful when an operation can produce multiple results:

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");
```

Conceptually:

```text
Mono
 │
 └── 0 or 1 value


Flux
 │
 ├── 0 values
 ├── 1 value
 ├── 2 values
 └── many values
```

The previous `Mono` example explores zero-or-one publishers in more detail.

---

## Creating a Flux

### `Flux.just()`

`Flux.just()` creates a `Flux` from explicitly provided values.

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");
```

The values are emitted in the order in which they are provided:

```text
John
Jane
Peter
```

The sequence then completes.

---

## Creating a Sequence with `range()`

`Flux.range()` creates a sequence of consecutive integers.

```java
Flux<Integer> numbers = Flux.range(1, 5);
```

This produces:

```text
1
2
3
4
5
```

The first argument specifies the starting value.

The second argument specifies how many values should be emitted.

For example:

```java
Flux.range(10, 3);
```

produces:

```text
10
11
12
```

---

## Creating a Flux from an Iterable

Existing Java collections can be adapted into a `Flux` using `fromIterable()`.

```java
List<String> names = List.of("John", "Jane", "Peter");

Flux<String> result = Flux.fromIterable(names);
```

Each element in the collection becomes an emitted value:

```text
List
 │
 ├── John
 ├── Jane
 └── Peter
 │
 ▼
Flux
```

This is useful when integrating existing collection-based data with a reactive pipeline.

---

## Transforming Values

The `map` operator transforms every value emitted by the `Flux`.

For example:

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");

Flux<String> uppercaseNames = names.map(String::toUpperCase);
```

The resulting sequence is:

```text
John   → JOHN
Jane   → JANE
Peter  → PETER
```

`map` transforms each value while preserving the number of emitted values.

Conceptually:

```text
Flux
 │
 ├── John
 ├── Jane
 └── Peter
       │
       ▼
      map
       │
       ▼
 ├── JOHN
 ├── JANE
 └── PETER
```

---

## Filtering Values

The `filter` operator allows values to continue through the pipeline only when they satisfy a condition.

```java
Flux<Integer> numbers = Flux.range(1, 6);

Flux<Integer> evenNumbers = numbers.filter(number -> number % 2 == 0);
```

The source sequence is:

```text
1
2
3
4
5
6
```

After filtering:

```text
2
4
6
```

Conceptually:

```text
1 ──┐
2 ──┼──► filter ──► 2
3 ──┤
4 ──┼──► filter ──► 4
5 ──┤
6 ──┼──► filter ──► 6
```

Unlike `map`, `filter` can change the number of values emitted by the resulting `Flux`.

---

## Limiting Values with `take()`

The `take` operator limits how many values are emitted by the resulting `Flux`.

```java
Flux<Integer> numbers = Flux.range(1, 10);

Flux<Integer> firstThree = numbers.take(3);
```

The source contains:

```text
1
2
3
4
5
6
7
8
9
10
```

The resulting `Flux` emits:

```text
1
2
3
```

and then completes.

Conceptually:

```text
1 ──►
2 ──►
3 ──► take(3)
4 ──►      │
5 ──►      └──► complete
...
10 ─►
```

This is useful when only the first part of a reactive sequence is required.

---

## Empty Flux

A `Flux` can complete without emitting any values.

Use `Flux.empty()` to create an empty publisher:

```java
Flux<String> result = Flux.empty();
```

The resulting sequence contains no `onNext` signals.

It simply completes:

```text
Flux.empty()
     │
     ▼
onComplete
```

This is similar to `Mono.empty()`, but represents an empty multi-value sequence.

---

## Collecting Values

A `Flux` can be converted into a `List` using `collectList()`.

For example:

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");

List<String> result = names.map(String::toUpperCase).collectList().block();
```

The reactive sequence:

```text
JOHN
JANE
PETER
```

is collected into:

```text
[JOHN, JANE, PETER]
```

Conceptually:

```text
Flux
 │
 ├── JOHN
 ├── JANE
 └── PETER
       │
       ▼
 collectList()
       │
       ▼
    List<String>
```

`collectList()` waits for the source `Flux` to complete before producing the resulting list.

In production reactive applications, blocking should generally be avoided. The `block()` call here is used only to keep the standalone example simple and make the resulting collection easy to demonstrate.

---

## Lazy Execution

Reactor publishers are lazy.

Defining a `Flux` does not necessarily mean that the work represented by the publisher has already been executed.

`Flux.defer()` can be used when the publisher itself should be created only when a subscriber subscribes.

```java
boolean[] executed = {false};

Flux<String> names = Flux.defer(() -> {
    executed[0] = true;

    return Flux.just("John", "Jane");
});
```

At this point, the supplier has not executed.

```text
Create Flux
     │
     ▼
No execution
```

When a subscriber subscribes:

```java
names.subscribe();
```

the supplier is invoked:

```text
subscribe()
     │
     ▼
Execute supplier
     │
     ▼
Create Flux
     │
     ├── John
     └── Jane
```

This demonstrates an important Reactor concept:

> A reactive pipeline describes work; subscription triggers execution.

---

## Testing Flux

Reactor provides `StepVerifier` for testing reactive publishers.

For example:

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");

StepVerifier.create(names).expectNext("John", "Jane", "Peter").verifyComplete();
```

`StepVerifier` allows tests to verify:

- emitted values
- value ordering
- completion
- empty sequences
- reactive signal behavior

For example, an empty `Flux` can be tested with:

```java
Flux<String> result = Flux.empty();

StepVerifier.create(result).verifyComplete();
```

The test verifies that the publisher completes without emitting any values.

---

## Complete Example

The concepts introduced in this example can be combined into a simple Flux pipeline:

```java
Flux<String> result = Flux.just("john", "jane", "peter")
        .map(String::trim)
        .filter(name -> !name.isEmpty())
        .map(String::toUpperCase)
        .take(2);
```

The data flows through the pipeline:

```text
"john"
"jane"
"peter"
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
 take(2)
   │
   ▼
"JOHN"
"JANE"
```

The resulting `Flux` emits two values and then completes.

---

## Key Concepts

### `Flux`

A Reactor publisher that can emit zero or more values.

### `Flux.just()`

Creates a `Flux` from explicitly provided values.

### `Flux.range()`

Creates a sequence of consecutive integers.

### `Flux.fromIterable()`

Adapts an existing `Iterable` into a reactive publisher.

### `Flux.empty()`

Creates a `Flux` that completes without emitting any values.

### `map`

Transforms every value emitted by the source publisher.

### `filter`

Allows only values satisfying a condition to continue through the pipeline.

### `take`

Limits the number of values emitted by the resulting publisher.

### `collectList`

Collects all emitted values into a `List` after the source completes.

### `Flux.defer()`

Delays creation of the source publisher until subscription.

### `StepVerifier`

Provides a way to verify values and terminal signals emitted by reactive publishers.

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

You can also run `FluxApplication` directly from your IDE.

---

## Key Takeaways

1. `Flux` represents zero or more values.
2. `Flux.just()` creates a publisher from explicitly provided values.
3. `Flux.range()` creates a sequence of consecutive integers.
4. `Flux.fromIterable()` adapts existing collections into reactive sequences.
5. `map` transforms every emitted value.
6. `filter` can remove values from a sequence.
7. `take` limits how many values are emitted.
8. `Flux.empty()` represents an empty reactive sequence.
9. `collectList()` collects a multi-value sequence into a `List`.
10. `Flux.defer()` demonstrates lazy publisher creation.
11. `StepVerifier` can verify reactive values and completion.

The central idea is:

```text
             Flux
              │
       ┌──────┼──────┐
       ▼      ▼      ▼
    value   value   value
       │      │      │
       └──────┼──────┘
              ▼
          onComplete
```

A `Flux` is therefore useful whenever an operation can produce **multiple results over time**.

---

## Next

**Reactive Pipelines**

The next example builds on `Mono` and `Flux` and focuses specifically on composing multiple Reactor operators into expressive reactive pipelines.
