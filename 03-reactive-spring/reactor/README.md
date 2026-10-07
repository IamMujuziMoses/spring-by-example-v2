# Reactor

Project Reactor is the reactive library that provides the foundation for reactive programming in the Spring ecosystem.

This example introduces the fundamentals of Reactor and reactive programming using small, focused examples covering reactive publishers, operators, pipelines, subscriptions, and lazy execution.

The example intentionally focuses on Reactor itself before introducing `Mono`, `Flux`, Spring WebFlux, reactive clients, and reactive data access in later examples.

## Learning Objectives

By completing this example, you will understand:

- The fundamentals of reactive programming
- The role of Project Reactor in the Spring ecosystem
- How reactive publishers represent data
- How to create reactive publishers
- How Reactor operators transform and process data
- How reactive operators can be composed into pipelines
- The role of subscribers
- Why Reactor pipelines are lazy
- How to test reactive publishers with `StepVerifier`

---

## Reactive Programming

Traditional imperative code generally processes data synchronously:

```text
Request
   │
   ▼
Process Data
   │
   ▼
Return Result
```

Reactive programming instead represents data as a stream that can be processed asynchronously and non-blockingly:

```text
Publisher
    │
    ▼
Reactive Pipeline
    │
    ├── Transform
    │
    ├── Filter
    │
    └── Process
    │
    ▼
Subscriber
```

Project Reactor provides the programming model used to build these reactive pipelines.

---

## Project Reactor

Reactor provides reactive types and operators for composing asynchronous processing pipelines.

A simple publisher can be created using:

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");
```

The publisher represents a sequence of values that can be consumed by a subscriber.

A simple pipeline can then transform the values:

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");

Flux<String> uppercaseNames = names.map(String::toUpperCase);
```

The pipeline can then be subscribed to:

```java
uppercaseNames.subscribe(System.out::println);
```

The output is:

```text
JOHN
JANE
PETER
```

---

## Publishers

A publisher represents a source of data that can be consumed reactively.

For example:

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");
```

The publisher does not necessarily execute its processing immediately.

Instead, Reactor allows the processing pipeline to be described first and executed when a subscriber subscribes.

This separation between **defining a pipeline** and **executing a pipeline** is an important part of reactive programming.

---

## Operators

Reactor provides operators for transforming and processing reactive data.

For example, `map` can transform each value:

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");

Flux<String> result = names.map(String::toUpperCase);
```

`filter` can select only values that match a condition:

```java
Flux<Integer> numbers = Flux.just(1, 2, 3, 4, 5);
Flux<Integer> evenNumbers = numbers.filter(number -> number % 2 == 0);
```

Operators can be combined to create more complex processing pipelines.

---

## Reactive Pipelines

A reactive pipeline is created by composing Reactor operators.

For example:

```java
Flux<Integer> numbers = Flux.range(1, 10);

Flux<Integer> result = numbers.filter(number -> number % 2 == 0).map(number -> number * 10);
```

The pipeline can be visualized as:

```text
1  2  3  4  5  6  7  8  9  10
│  │  │  │  │  │  │  │  │   │
└──┴──┴──┴──┴──┴──┴──┴──┴───┘
             │
             ▼
          filter
             │
             ▼
        2  4  6  8  10
             │
             ▼
            map
             │
             ▼
      20  40  60  80  100
```

The operators describe what should happen to the data as it flows through the pipeline.

---

## Subscription

A reactive pipeline needs a subscriber to consume its signals.

For example:

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");

names.map(String::toUpperCase).subscribe(System.out::println);
```

The `subscribe` operation connects a consumer to the reactive pipeline.

Conceptually:

```text
Publisher
    │
    ▼
Operators
    │
    ▼
Pipeline
    │
    ▼
Subscriber
```

---

## Lazy Execution

Reactor pipelines are lazy by default.

Defining a pipeline does not necessarily mean that the processing has already happened.

For example:

```java
Flux<String> names = Flux.defer(() -> {System.out.println("Pipeline executed");return Flux.just("John", "Jane");});
```

At this point, the pipeline has been defined but has not been subscribed to.

Execution occurs when a subscriber subscribes:

```java
names.subscribe(System.out::println);
```

This produces:

```text
Pipeline executed
John
Jane
```

This distinction is important when working with reactive systems:

```text
Define Pipeline
      │
      ▼
    Nothing
      │
      │ subscribe()
      ▼
Execute Pipeline
      │
      ▼
Process Data
```

---

## Testing Reactive Publishers

Reactor provides `StepVerifier` for testing reactive sequences.

For example:

```java
Flux<String> names = Flux.just("John", "Jane", "Peter");

StepVerifier.create(names).expectNext("John", "Jane", "Peter").verifyComplete();
```

`StepVerifier` can verify:

- emitted values
- completion signals
- error signals
- the order of emitted values
- reactive sequence behavior

This makes it possible to test reactive pipelines without manually subscribing and collecting results.

---

## Example Pipeline

The example combines several Reactor concepts:

```java
Flux<Integer> numbers = Flux.range(1, 10);
Flux<Integer> result = numbers.filter(number -> number % 2 == 0).map(number -> number * 10);

StepVerifier.create(result).expectNext(20, 40, 60, 80, 100).verifyComplete();
```

The flow is:

```text
Flux.range()
     │
     ▼
   filter
     │
     ▼
    map
     │
     ▼
StepVerifier
     │
     ▼
  verifyComplete()
```

---

## Reactor and Spring

Project Reactor is an important foundation for Spring's reactive programming model.

It is used by technologies such as:

- Spring WebFlux
- Reactive Spring Data
- `WebClient`

The relationship can be summarized as:

```text
                 Project Reactor
                       │
          ┌────────────┼────────────┐
          │            │            │
          ▼            ▼            ▼
     Spring WebFlux  WebClient  Spring Data
          │            │            │
          └────────────┼────────────┘
                       │
                Reactive Applications
```

The following examples in this module build on these Reactor fundamentals.

---

## Key Concepts

### Publisher

Represents a source of data that can emit values over time.

### Operator

Defines how data should be transformed, filtered, combined, or otherwise processed.

### Subscriber

Consumes the signals emitted by a publisher.

### Reactive Pipeline

A sequence of operators that describes how data flows through a reactive process.

### Subscription

Connects a subscriber to a publisher and triggers execution of a lazy pipeline.

### Lazy Execution

Reactive processing is generally not performed simply because a pipeline has been defined. Subscription triggers execution.

### `StepVerifier`

A Reactor testing utility used to verify reactive publishers and their emitted signals.

---

## Dependencies

The example uses Project Reactor directly without Spring Boot or Spring WebFlux.

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

You can also run the `ReactorApplication` class directly from your IDE.

---

## Key Takeaways

1. Project Reactor provides the foundation for reactive programming in Spring.
2. Reactive publishers represent data that can be consumed asynchronously.
3. Operators allow reactive data to be transformed and processed.
4. Operators can be composed into reactive pipelines.
5. Reactive pipelines are lazy by default.
6. Subscription triggers execution of a reactive pipeline.
7. `StepVerifier` provides a convenient way to test reactive publishers.
8. Reactor fundamentals provide the foundation for Spring WebFlux and reactive Spring Data.

The central idea is:

```text
Publisher
    │
    ▼
Operators
    │
    ▼
Reactive Pipeline
    │
    ▼
Subscription
    │
    ▼
Execution
```

---

## Next

**Mono**

The next example explores `Mono`, Reactor's publisher type for producing zero or one value.
