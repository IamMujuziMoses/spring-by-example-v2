# Reactive Testing

This module demonstrates how to test reactive applications using **Project Reactor, Reactor Test, and Spring WebFlux testing support**.

Reactive applications introduce testing challenges that differ from traditional synchronous applications. Publishers can emit multiple values asynchronously, terminate with errors, complete without emitting values, or respond to downstream demand.

The goal of this example is to understand how to test reactive behavior without unnecessarily blocking, verify publisher signals, control time-dependent tests, and validate HTTP interactions in Spring WebFlux.

## Learning Objectives

By completing this example, you should understand:

- How to test `Mono` and `Flux` publishers using `StepVerifier`.
- How to verify emitted values, ordering, and completion.
- How to test reactive error signals and recovery behavior.
- How to test time-based publishers using virtual time.
- How to verify backpressure and demand-driven emissions.
- How to test WebFlux controllers using `WebTestClient`.
- How to isolate controller tests by mocking service dependencies.
- How to test reactive service behavior without starting a web server.
- Why reactive tests should avoid unnecessary blocking operations.

---

## 1. Understanding Reactive Testing

In traditional synchronous code, a test often calls a method, obtains its return value, and makes assertions against that value.

Reactive code works differently. A `Mono` or `Flux` represents a publisher that emits signals when subscribed to.

A reactive publisher can emit three kinds of signals:

- **Next:** An item is available.
- **Complete:** The publisher has completed successfully.
- **Error:** The publisher has terminated because of a failure.

For example:

```java
Mono<String> greeting = Mono.just("Hello, Reactor!");
```

Instead of blocking to retrieve the value, a test can subscribe to the publisher and verify its signals with `StepVerifier`.

This allows the test to validate both the data and the lifecycle of the reactive sequence.

---

## 2. Testing Publishers with `StepVerifier`

`StepVerifier` is provided by the `reactor-test` library. It lets tests describe the expected signals from a publisher in sequence.

### 2.1. Testing a `Mono`

A `Mono` emits zero or one item, followed by completion or an error.

```java
@Test
void shouldEmitGreeting() {
    StepVerifier.create(greetingService.greet("Moses"))
        .expectNext(new Greeting("Hello, Moses!"))
        .verifyComplete();
}
```

This test verifies that:

1. The publisher emits the expected greeting.
2. The publisher completes successfully.
3. No additional values are emitted before completion.

The test does not need to call `block()` to obtain the greeting.

### 2.2. Testing a `Flux`

A `Flux` can emit zero or more items.

```java
@Test
void shouldEmitAllGreetingsInOrder() {
    StepVerifier.create(greetingService.findAll())
        .expectNext(new Greeting("Hello, Spring!"))
        .expectNext(new Greeting("Hello, Reactor!"))
        .expectNext(new Greeting("Hello, WebFlux!"))
        .verifyComplete();
}
```

This verifies the emitted values in their expected order and confirms that the publisher completes successfully.

### 2.3. Common `StepVerifier` Assertions

| Assertion                       | Purpose                                                               |
|---------------------------------|-----------------------------------------------------------------------|
| `expectNext(value)`             | Expects a particular emitted value.                                   |
| `expectNextCount(count)`        | Expects a specified number of values.                                 |
| `assertNext(consumer)`          | Applies assertions to the next emitted item.                          |
| `expectComplete()`              | Expects successful completion.                                        |
| `expectError()`                 | Expects an error signal.                                              |
| `expectErrorMatches(predicate)` | Verifies an error using a predicate.                                  |
| `verifyComplete()`              | Subscribes and verifies successful completion.                        |
| `verify()`                      | Executes the verification and waits for the expected terminal signal. |

These assertions make reactive tests explicit about the behavior being verified.

---

## 3. Testing Time-Based Publishers

Reactive applications often use time-based operators such as `delayElements()`, `interval()`, and `timeout()`.

Waiting for real time to pass can make tests unnecessarily slow. Reactor Test provides virtual-time support for testing suitable time-based publishers.

```java
@Test
void shouldTestTimedPublisherUsingVirtualTime() {
    StepVerifier.withVirtualTime(() -> Flux.interval(Duration.ofSeconds(1)).take(3))
        .expectSubscription()
        .thenAwait(Duration.ofSeconds(3))
        .expectNext(0L, 1L, 2L)
        .verifyComplete();
}
```

This test verifies a publisher that emits three values at one-second intervals without waiting three seconds in real time.

### Why Use Virtual Time?

Virtual time is useful when testing:

- Delayed emissions.
- Periodic publishers.
- Time-based retries.
- Timeout behavior.
- Other publishers that use Reactor's virtual-time-compatible scheduling.

**Important:** Create the time-dependent publisher inside the supplier passed to `withVirtualTime()`. This allows the virtual scheduler to be installed before the publisher is assembled.

Virtual time is not a universal replacement for every timing test. Publishers that use independent schedulers or external asynchronous systems may require other testing strategies.

---

## 4. Testing Reactive Services

The example includes a `GreetingService` that returns `Mono<Greeting>` and `Flux<Greeting>` publishers.

```java
@Service
public class GreetingService {

    public Mono<Greeting> greet(String name) {
        return Mono.just(new Greeting("Hello, " + name + "!"));
    }

    public Flux<Greeting> findAll() {
        return Flux.just(
            new Greeting("Hello, Spring!"),
            new Greeting("Hello, Reactor!"),
            new Greeting("Hello, WebFlux!")
        );
    }

    public Mono<Greeting> findByName(String name) {
        return Mono.just(name).filter(value -> !value.isBlank()).map(value -> new Greeting("Hello, " + value + "!"))
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Name must not be blank")));
    }
}
```

The service tests instantiate the service directly and use `StepVerifier` to validate its publishers.

This approach is appropriate for a simple service that has no external dependencies.

For services that depend on repositories, HTTP clients, or other components, unit tests can mock those dependencies and verify the resulting reactive behavior.

---

## 5. Running the Example

From the repository root, run the module tests:

```bash
mvn -pl 03-reactive-spring/reactive-testing clean verify
```

Run only the service tests:

```bash
mvn -pl 03-reactive-spring/reactive-testing \
    -Dtest=GreetingServiceTest test
```

Run only the controller tests:

```bash
mvn -pl 03-reactive-spring/reactive-testing \
    -Dtest=GreetingControllerTest test
```

Start the application:

```bash
mvn -pl 03-reactive-spring/reactive-testing spring-boot:run
```

Once the application is running, try the endpoints:

```bash
curl http://localhost:8080/api/greetings
curl "http://localhost:8080/api/greetings/personal?name=Moses"
curl http://localhost:8080/api/greetings/Reactor
```

---

## 6. Key Takeaways

- `StepVerifier` provides a declarative way to test reactive publisher signals.
- `Mono` and `Flux` tests should verify emitted values and terminal signals.
- Reactive errors can be tested without blocking to retrieve results.
- `withVirtualTime()` can make suitable time-based tests faster and more deterministic.
- Explicit requests in `StepVerifier` help test backpressure behavior.
- `WebTestClient` validates the HTTP contract of a WebFlux application.
- Service tests and controller tests cover different application boundaries.
- Avoid unnecessary `block()` calls when reactive assertions can verify publisher behavior directly.

---

## Conclusion

Reactive testing is essential for building reliable applications with Project Reactor and Spring WebFlux.

By learning to verify values, completion, errors, timing, and backpressure, developers can test reactive pipelines without abandoning the reactive programming model. Combining `StepVerifier` with `WebTestClient` also provides coverage at both the publisher level and the HTTP boundary.

This example provides a foundation for writing clear, focused, and maintainable tests for reactive applications.

---

## Next Example

The next example moves beyond reactive programming and begins **Module 04 — Spring Modulith**.

The focus will shift from building reactive applications to modular application design, exploring how Spring Modulith helps structure Spring Boot applications into well-defined, loosely coupled modules.
