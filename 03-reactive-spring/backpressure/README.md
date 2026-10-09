# Backpressure

This example demonstrates **backpressure in Project Reactor**.

It shows how reactive subscribers can control the amount of data they receive by managing demand, how requests can be made incrementally, and how Reactor provides operators for coordinating upstream demand and buffering values.

## What This Example Demonstrates

- Reactive Streams backpressure
- Subscriber demand
- `Subscription.request(n)`
- Custom demand management with `BaseSubscriber`
- Incremental element requests
- Testing backpressure with `StepVerifier`
- Upstream request management with `limitRate()`
- Buffering with `onBackpressureBuffer()`
- Difference between demand management and buffering
- Testing reactive sequences with controlled demand

---

## What Is Backpressure?

Backpressure is a mechanism that allows a subscriber to communicate how many elements it is ready to receive from a publisher.

In reactive programming, a publisher may produce data faster than a subscriber can process it. Backpressure provides a demand-based mechanism that helps coordinate the flow of data between publishers and subscribers.

For example:

```java
Flux<Integer> numbers = Flux.range(1, 10);
```

The publisher represents ten integers, but the subscriber does not have to request all ten elements at once.

It can request two elements first:

```java
subscription.request(2);
```

Then request more when it is ready:

```java
subscription.request(3);
```

The subscriber's demand determines how many additional elements it can receive.

---

## Reactive Streams Demand

Reactive Streams defines a protocol for coordinating data exchange between publishers and subscribers.

The `Subscription` interface provides two important methods:

```java
public interface Subscription {

    void request(long n);

    void cancel();
}
```

### `request(n)`

Requests additional elements from the publisher.

```java
subscription.request(2);
```

This adds two elements to the subscriber's outstanding demand.

Calling `request(n)` does not guarantee that the elements will be delivered immediately. It communicates demand to the publisher.

### `cancel()`

Cancels the subscription when the subscriber no longer wants to receive elements.

In Reactor, cancellation can also be performed through operators such as `take()` or through subscriber cancellation.

---

## Understanding Demand

Suppose a publisher emits the numbers from `1` to `5`.

The subscriber starts with no demand and requests elements incrementally.

```text
Publisher: Flux.range(1, 5)
                  │
                  ▼
             Subscription
                  │
                  ▼
          Subscriber demand = 0
                  │
             request(2)
                  │
                  ▼
             Emits 1, 2
                  │
             request(1)
                  │
                  ▼
               Emits 3
                  │
             request(2)
                  │
                  ▼
              Emits 4, 5
                  │
                  ▼
              Completed
```

The subscriber requests five elements in three separate requests.

This demonstrates that demand can be managed incrementally rather than requesting every element at the beginning.

---

## Using `BaseSubscriber`

Reactor provides `BaseSubscriber` for implementing custom subscriber behavior.

It exposes lifecycle hooks that allow a subscriber to control when it requests elements.

For example:

```java
Flux.range(1, 10).subscribe(new BaseSubscriber<Integer>() {

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(2);
            }

            @Override
            protected void hookOnNext(Integer value) {
                System.out.println("Received: " + value);

                if (value == 2) {
                    request(3);
                } else if (value == 5) {
                    request(5);
                }
            }
        });
```

The subscriber requests elements in batches.

The first request asks for two elements. After receiving the second element, the subscriber requests three more. After receiving the fifth element, it requests the remaining five.

The resulting output is:

```text
Received: 1
Received: 2
Received: 3
Received: 4
Received: 5
Received: 6
Received: 7
Received: 8
Received: 9
Received: 10
```

The `BaseSubscriber` hooks used here are:

- `hookOnSubscribe()` — called when the subscription is established.
- `hookOnNext()` — called when an element is received.

This approach provides explicit control over demand without relying on the deprecated four-argument `subscribe()` overload.

---

## Managing Upstream Requests with `limitRate()`

The `limitRate()` operator reshapes demand before forwarding requests to an upstream publisher.

For example:

```java
Flux<Integer> numbers = Flux.range(1, 10)
        .limitRate(3);
```

Here, the operator manages upstream requests in smaller batches rather than simply forwarding all downstream demand as one request.

A simplified view is:

```text
Downstream Subscriber
          │
          │ Requests elements
          ▼
      limitRate(3)
          │
          │ Manages upstream request batches
          ▼
    Upstream Publisher
```

This can be useful when controlling the size of upstream request batches.

**Important:** `limitRate()` does not slow down the downstream subscriber or impose a time-based rate limit. It manages how demand is propagated upstream.

---

## Buffering with `onBackpressureBuffer()`

Sometimes a publisher produces values that cannot immediately be delivered downstream because the subscriber has insufficient demand.

The `onBackpressureBuffer()` operator provides a way to buffer such values.

```java
Flux<Integer> numbers = Flux.range(1, 5)
        .onBackpressureBuffer();
```

A simplified view is:

```text
Upstream Publisher
        │
        │ Produces values
        ▼
   Backpressure Buffer
        │
        │ Values wait when downstream demand is insufficient
        ▼
 Downstream Subscriber
        │
        │ Requests elements
        ▼
  Receives buffered values
```

For example, a subscriber can request two values first and then request the remaining three.

```java
@Test
void backpressureBuffer_shouldDeliverValuesAsDemandArrives() {
    // The operator buffers values that cannot immediately be
    // delivered downstream because of insufficient demand.
    Flux<Integer> numbers = Flux.range(1, 5)
            .onBackpressureBuffer();

    StepVerifier.create(numbers, 0)
            .thenRequest(2)
            .expectNext(1, 2)
            .thenRequest(3)
            .expectNext(3, 4, 5)
            .verifyComplete();
}
```

This test verifies that the subscriber can receive the complete sequence as demand becomes available.

It does not, by itself, prove that the upstream publisher produced values without demand. That behavior depends on the upstream publisher and how the backpressure operator requests data.

### Buffering Considerations

Buffering is not the same as slowing down the producer.

- A buffer stores values until they can be delivered downstream.
- A buffer can consume increasing amounts of memory.
- An unbounded buffer can become problematic when production consistently outpaces consumption.
- Bounded buffering with an explicit overflow strategy may be more appropriate for production workloads.

Backpressure and buffering solve related but different problems.

---

## Backpressure Flow

The overall demand-management process can be simplified as follows:

```text
Publisher
    │
    ▼
Subscription Established
    │
    ▼
Subscriber Requests Elements
    │
    ▼
Publisher Responds to Demand
    │
    ▼
Subscriber Processes Values
    │
    ├── Needs More Values
    │       │
    │       ▼
    │   request(n)
    │
    └── No Longer Needs Values
            │
            ▼
        Cancel Subscription
```

The key idea is that a subscriber can communicate its demand and cancel when it no longer needs the stream.

Operators such as `limitRate()` and `onBackpressureBuffer()` provide additional ways to manage how data moves through a reactive pipeline.

---

## Key Concepts

### `Subscription.request(n)`

Requests additional elements from the publisher.

```java
subscription.request(3);
```

The request increases the subscriber's outstanding demand.

### `BaseSubscriber`

Provides hooks for custom subscriber behavior.

```java
@Override
protected void hookOnSubscribe(Subscription subscription) {
    request(2);
}
```

### `StepVerifier`

Tests reactive sequences with controlled demand.

```java
StepVerifier.create(numbers, 0)
        .thenRequest(2)
        .expectNext(1, 2)
        .thenCancel()
        .verify();
```

### `limitRate()`

Reshapes downstream demand into smaller upstream request batches.

```java
Flux.range(1, 10).limitRate(3);
```

### `onBackpressureBuffer()`

Buffers values when they cannot immediately be delivered downstream because of insufficient demand.

```java
Flux.range(1, 10).onBackpressureBuffer();
```

### Cancellation

Stops the subscriber from requesting or receiving further elements through that subscription.

```java
subscription.cancel();
```

---

## What This Example Does Not Cover

This example intentionally focuses on backpressure fundamentals.

It does not cover:

- Time-based rate limiting
- Hot publishers and multicast backpressure
- Bounded buffer overflow strategies
- Dropping or latest-value backpressure strategies
- Asynchronous producer-consumer pipelines
- Custom Reactive Streams publishers
- Backpressure in Spring WebFlux applications
- Production monitoring and overload management

These concepts can be explored separately when needed.

---

## Running the Example

From the module directory:

```bash
mvn clean test
```

Or from the project root:

```bash
mvn -pl 03-reactive-spring/backpressure test
```

To run the complete project build:

```bash
mvn clean install
```

To run the console demonstration, execute `BackpressureApplication.main()` from your IDE or use your preferred Java execution workflow.

The application uses `BaseSubscriber` to request elements incrementally and `.log()` to expose Reactor signals in the console.

---

## Running the Tests

Run:

```bash
mvn test
```

The tests verify:

- Emitting only the requested number of values
- Incrementally increasing demand
- Completing after all requested values are consumed
- Managing upstream requests with `limitRate()`
- Delivering buffered values as downstream demand arrives
- Cancelling a subscription after consuming a subset of values

---

## Key Takeaways

- Backpressure allows subscribers to communicate demand to publishers.
- `Subscription.request(n)` requests additional elements.
- Demand can be managed incrementally.
- `BaseSubscriber` provides hooks for custom demand management.
- `StepVerifier` can test reactive sequences with controlled demand.
- `limitRate()` reshapes upstream requests.
- `onBackpressureBuffer()` buffers values when downstream demand is insufficient.
- Buffering does not slow down the upstream producer and can consume memory.
- Cancellation allows a subscriber to stop consuming a sequence.
- Backpressure, buffering, and time-based rate limiting are different concepts.

---

## Next Example

The next example will explore **Spring WebFlux**, introducing Spring's reactive web framework and demonstrating how Reactor publishers are used to build reactive HTTP applications.