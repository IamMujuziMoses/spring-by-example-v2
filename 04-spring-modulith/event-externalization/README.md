# Event Externalization

This example demonstrates **event externalization** in Spring Modulith.

It focuses on how Spring Modulith identifies application events for externalization, how events are published to an external messaging system such as Apache Kafka, and how transaction synchronization and the event publication registry support the externalization process.

## What This Example Demonstrates

- What event externalization means in Spring Modulith.
- Domain events and Spring's `ApplicationEventPublisher`.
- The `@Externalized` annotation.
- Externalizing an event to a Kafka topic.
- The role of `KafkaEventExternalizerConfiguration`.
- How Spring Modulith represents externalized events as Spring `Message` objects.
- The `kafka_topic` message header.
- The role of `EventPublicationRegistry`.
- Transaction-aware event processing with `@Transactional`.
- Testing event externalization with JUnit 5 and Mockito.
- Testing the externalization call without requiring a running Kafka broker.

---

## What Is Event Externalization?

Event externalization is the process of forwarding an application event to an external system so that another application or service can consume it.

Within an application, Spring events provide a mechanism for publishing events and allowing interested components to respond to them.

However, an application event published through Spring's `ApplicationEventPublisher` is not automatically sent to an external messaging system.

Spring Modulith provides event externalization support that connects application events to external destinations.

A simplified flow looks like:

```text
Application Service
        |
        v
Application Event
        |
        v
Spring Modulith
        |
        v
Event Externalizer
        |
        v
External Message
        |
        v
Kafka Topic
        |
        v
External Consumer
```

This allows an application to publish domain events without directly coupling its business logic to Kafka APIs.

---

## Domain Events

A domain event represents something meaningful that has happened in an application.

For example, an order-processing application might publish events when an order is created, completed, or cancelled.

In this example, `OrderCompleted` represents the completion of an order.

```java
import org.springframework.modulith.events.Externalized;

@Externalized("orders.completed")
public record OrderCompleted(String orderId) {
}
```

The event contains the order ID associated with the completed order.

The `@Externalized` annotation tells Spring Modulith that this event should be externalized and identifies its destination.

In this example, the destination is:

```text
orders.completed
```

With the Kafka integration used here, this identifies the Kafka topic.

---

## The `@Externalized` Annotation

Spring Modulith provides `@Externalized` to mark application events for externalization.

For example:

```java
@Externalized("orders.completed")
public record OrderCompleted(String orderId) {
}
```

The annotation associates the event with an external destination.

Conceptually:

```text
OrderCompleted
       |
       v
@Externalized
       |
       v
orders.completed
       |
       v
Kafka Topic
```

This keeps the destination declaration close to the event definition.

The service publishing the event does not need to know which Kafka topic receives it or how the external message is constructed.

---

## Publishing the Event

The `OrderService` publishes an `OrderCompleted` event through Spring's `ApplicationEventPublisher`.

```java
@Service
public class OrderService {

    private final ApplicationEventPublisher eventPublisher;

    public OrderService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void completeOrder(String orderId) {
        eventPublisher.publishEvent(new OrderCompleted(orderId));
    }
}
```

When `completeOrder()` is called, the service creates and publishes the domain event.

```java
orderService.completeOrder("ORDER-001");
```

The service does not call `KafkaOperations` directly.

Instead, Spring Modulith detects the event, identifies its externalization configuration, and processes it through the configured externalization infrastructure.

This separates the business operation from the messaging implementation.

---

## Why `@Transactional`?

This example marks `completeOrder()` with `@Transactional`.

```java
@Transactional
public void completeOrder(String orderId) {
    eventPublisher.publishEvent(new OrderCompleted(orderId));
}
```

Spring Modulith's event externalization listener uses transaction-aware processing.

When an event is published inside an active transaction, Spring can register transaction synchronization so the listener is triggered at the appropriate transaction stage.

The simplified flow is:

```text
Transactional Service Method
             |
             v
Publish Application Event
             |
             v
Register Transaction Synchronization
             |
             v
Transaction Reaches Relevant Stage
             |
             v
Event Externalization Listener
             |
             v
Externalize Event
```

Without an active transaction, transaction-aware listeners configured to require one may skip processing.

The transaction also provides a useful boundary when the business operation updates database state and publishes an event.

However, transaction management alone does not guarantee successful delivery to Kafka. External system failures, retries, broker acknowledgements, and consumer processing require their own considerations.

---

## Event Publication Registry

Spring Modulith provides an `EventPublicationRegistry` to track event publications and their processing status.

When an event is published for a listener, the registry records the publication. After the listener successfully completes its processing, the publication can be marked as completed.

Conceptually:

```text
Event Published
      |
      v
Publication Registered
      |
      v
Listener Processes Event
      |
      v
Publication Completed
```

The registry helps Spring Modulith track which event publications have been processed and which remain unfinished.

This is useful when diagnosing event-processing problems and building applications that need reliable event-publication workflows.

The registry is not itself a Kafka producer. It tracks event publication and listener processing, while the Kafka integration performs the externalization.

---

## JDBC Event Publication Registry

This example uses Spring Modulith's JDBC integration and an H2 database for testing.

The test configuration includes:

```properties
spring.datasource.url=jdbc:h2:mem:event-externalization;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

spring.modulith.events.jdbc.schema-initialization.enabled=true
```

The JDBC infrastructure provides persistence for event publication records.

The in-memory H2 database makes the example easy to run in tests without requiring an external database server.

Because the database is in-memory, its publication records are not intended to survive the lifetime of the test process. A production application should use a persistent database and an appropriate schema-management strategy.

---

## Kafka Event Externalization

This example includes Spring Modulith's Kafka event externalization integration.

The externalization configuration detects events marked for externalization and forwards them through `KafkaOperations`.

The relevant destination is:

```text
orders.completed
```

A simplified view is:

```text
OrderCompleted
      |
      v
Spring Modulith Event Externalizer
      |
      v
KafkaOperations
      |
      v
Kafka Message
      |
      v
orders.completed
```

The event publisher and the externalization infrastructure have separate responsibilities:

- `ApplicationEventPublisher` publishes the application event.
- Spring Modulith identifies and processes the externalized event.
- The Kafka integration constructs and sends the external message.
- Kafka provides the messaging infrastructure for consumers.

The service therefore remains independent of the Kafka-specific publishing API.

---

## How the Externalized Message Is Represented

In this example, Spring Modulith's Kafka integration sends a Spring `Message` to `KafkaOperations`.

The destination is carried in the message's `kafka_topic` header.

Conceptually, the message looks like:

```text
Message
  |
  +-- Payload
  |     |
  |     +-- OrderCompleted[orderId=ORDER-001]
  |
  +-- Headers
        |
        +-- kafka_topic = orders.completed
```

This is important when testing the externalization call.

The integration invokes the single-argument `send(Message<?>)` overload, rather than necessarily invoking an overload such as `send(topic, key, value)`.

A test should therefore inspect the message's headers and payload instead of assuming a particular multi-argument Kafka method is called.

---

## Testing Event Externalization

The example uses Spring Boot's test support, JUnit 5, and Mockito.

The integration test calls the service:

```java
orderService.completeOrder("ORDER-001");
```

It then verifies that the Kafka integration receives a message with the expected topic and payload.

A representative verification is:

```java
verify(kafkaOperations, timeout(5000))
        .send(argThat((Message<?> message) ->"orders.completed".equals(message.getHeaders().get("kafka_topic"))
        && message.getPayload() instanceof OrderCompleted
        && "ORDER-001".equals(((OrderCompleted) message.getPayload()).orderId())));
```

The verification checks three things:

1. The message is addressed to `orders.completed`.
2. The payload is an `OrderCompleted` event.
3. The event contains the expected order ID.

The `timeout(5000)` verification allows Mockito to wait for the asynchronous externalization call for up to five seconds.

Because the externalizer may run asynchronously, the test should not assume that the send call happens immediately on the test thread.

---

## Why Mock `KafkaOperations`?

The example uses a mocked `KafkaOperations` bean.

This makes it possible to verify the behavior of Spring Modulith's externalization infrastructure without starting a real Kafka broker.

The mock acts as the boundary between Spring Modulith and the external messaging system.

Conceptually:

```text
OrderService
     |
     v
Spring Modulith
     |
     v
Kafka Event Externalizer
     |
     v
Mock KafkaOperations
     |
     v
Verify Message
```

The mock allows the test to verify that the correct message is passed to the Kafka integration.

However, it does not verify that a real Kafka broker accepts the message, that the message is persisted by the broker, or that a consumer can receive it.

Those behaviors would require a separate integration test using a real or test-managed Kafka broker.

---

## Event Externalization Flow

A simplified view of the complete example is:

```text
OrderService.completeOrder()
             |
             v
ApplicationEventPublisher
             |
             v
OrderCompleted
             |
             v
Spring Modulith Event Publication Registry
             |
             v
Transaction Synchronization
             |
             v
Event Externalizer
             |
             v
Spring Message
             |
             +-- Payload: OrderCompleted
             |
             +-- Header: kafka_topic
                          |
                          v
                    KafkaOperations
                          |
                          v
                  orders.completed
```

This flow illustrates how a domain event can be published within an application and forwarded to an external messaging destination without requiring the service itself to depend on Kafka.

---

## Event Externalization vs Direct Kafka Publishing

These approaches solve related but different problems.

### Direct Kafka Publishing

```text
OrderService
     |
     v
KafkaOperations
     |
     v
Kafka Topic
```

The service directly depends on Kafka's publishing API and must know how to construct and send the message.

### Spring Modulith Event Externalization

```text
OrderService
     |
     v
ApplicationEventPublisher
     |
     v
Spring Modulith
     |
     v
Kafka Integration
     |
     v
Kafka Topic
```

The service publishes a domain event, while Spring Modulith handles the externalization.

This separation can make the business logic easier to test and less dependent on the messaging technology.

---

## Running the Example

From the repository root:

```bash
./mvnw -pl 04-spring-modulith/event-externalization spring-boot:run
```

This command runs the module's application if the module is configured with a runnable Spring Boot application.

For this example, the primary demonstration is the integration test, which publishes an event and verifies the externalization call.

---

## Running the Tests

Run the tests from the module:

```bash
./mvnw -pl 04-spring-modulith/event-externalization test
```

Or include the module's required Maven dependencies from the repository root:

```bash
./mvnw -pl 04-spring-modulith/event-externalization -am test
```

The test uses an H2 database and a mocked `KafkaOperations` bean. A running Kafka broker is not required.

---

## Key Takeaways

- Event externalization forwards application events to external messaging destinations.
- `@Externalized` identifies events for externalization and specifies the destination.
- `ApplicationEventPublisher` allows services to publish application events without directly depending on Kafka.
- `KafkaEventExternalizerConfiguration` connects Spring Modulith's externalization infrastructure to Kafka.
- The externalized message carries its destination in the `kafka_topic` header.
- The event publication registry tracks the processing status of event publications.
- Transaction synchronization affects when transaction-aware event listeners process events.
- Asynchronous event processing means tests may need to wait for the externalization call.
- Mocking `KafkaOperations` verifies the externalization call without requiring a broker.
- A mocked Kafka integration does not prove actual broker delivery or downstream consumption.

---

## What This Example Does Not Cover

This example intentionally focuses on the basic event externalization workflow.

It does not cover:

- Publishing to a running Kafka broker.
- Consuming externalized events from another application.
- End-to-end Kafka integration testing.
- Kafka retry and dead-letter topic strategies.
- Broker acknowledgements and delivery guarantees.
- Duplicate event handling and idempotent consumers.
- Schema evolution and message versioning.
- Production database configuration and operational monitoring.
- Distributed transactions between a database and Kafka.

These topics can be explored separately when needed.

---

## Next Example

**Module verification**

The next example will explore another Spring Modulith concept, building on the separation between application modules, domain events, and external messaging infrastructure.
