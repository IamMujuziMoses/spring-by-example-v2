# Spring Cloud Messaging

This example demonstrates **message-driven communication between Spring Boot services using Spring Cloud Stream and RabbitMQ**.

It focuses on how services communicate asynchronously through messages instead of directly invoking one another over HTTP.

The example consists of an Order Service that publishes an order-created event and an Inventory Service that consumes the event and updates its inventory.

It also demonstrates Spring Cloud Stream's functional programming model, `StreamBridge`, RabbitMQ message bindings, and integration testing with Testcontainers.

## What This Example Demonstrates

- What message-driven communication means in a microservices architecture.
- Synchronous vs asynchronous communication.
- Spring Cloud Stream.
- The Spring Cloud Stream functional programming model.
- Publishing messages using `StreamBridge`.
- Consuming messages using `Consumer<T>`.
- Message destinations and bindings.
- RabbitMQ as a message broker.
- JSON message serialization and deserialization.
- Consumer groups.
- Asynchronous event processing.
- Testing message-driven communication with RabbitMQ Testcontainers.
- Verifying asynchronous processing with Awaitility.

---

## What Is Message-Driven Communication?

Message-driven communication is an approach where services communicate by exchanging messages through a message broker.

Instead of one service directly invoking another service, the sending service publishes a message to a destination. The receiving service subscribes to that destination and processes the message independently.

For example, when an order is created, the Order Service publishes an `OrderCreatedEvent`.

The Inventory Service consumes that event and updates the available inventory.

A simplified flow looks like:

```text
Client
  │
  ▼
Order Service
  │
  │ Publish OrderCreatedEvent
  ▼
RabbitMQ
  │
  │ Deliver Message
  ▼
Inventory Service
  │
  ▼
Update Inventory
```

The Order Service does not need to know which services consume the event.

This reduces direct dependencies between services and allows them to evolve independently.

---

## Synchronous vs Asynchronous Communication

Microservices commonly communicate using synchronous or asynchronous communication.

### Synchronous Communication

In synchronous communication, one service directly calls another service and waits for its response.

For example:

```text
Client
  │
  ▼
Order Service
  │
  │ HTTP Request
  ▼
Inventory Service
  │
  │ HTTP Response
  ▼
Order Service
  │
  ▼
Client
```

The Order Service depends on the Inventory Service being available to complete the request.

If the Inventory Service is unavailable or slow, the Order Service may also experience delays or failures.

### Asynchronous Communication

In asynchronous communication, the sending service publishes a message and does not wait for the receiving service to finish processing it.

```text
Client
  │
  ▼
Order Service
  │
  │ Publish Event
  ▼
RabbitMQ
  │
  │
  ▼
Order Service Returns Response

       ...

RabbitMQ
  │
  │ Deliver Event
  ▼
Inventory Service
  │
  ▼
Update Inventory
```

The Order Service and Inventory Service can operate independently.

The message broker acts as an intermediary between them.

---

## Spring Cloud Stream

Spring Cloud Stream is a framework for building message-driven applications.

It provides an abstraction over messaging systems such as RabbitMQ and Apache Kafka.

Instead of writing application code that directly interacts with a broker's client API, developers work with Spring Cloud Stream concepts such as:

- Message bindings.
- Destinations.
- Producers.
- Consumers.
- Functional message handlers.
- Message serialization and deserialization.

Spring Cloud Stream separates application logic from the underlying messaging infrastructure.

Conceptually:

```text
Application Code
      │
      ▼
Spring Cloud Stream
      │
      ▼
Message Binder
      │
      ▼
RabbitMQ / Kafka
```

A binder connects Spring Cloud Stream to a particular messaging system.

This example uses the RabbitMQ binder.

---

## RabbitMQ

RabbitMQ is a message broker that enables applications to exchange messages through messaging destinations.

In this example, RabbitMQ receives messages published by the Order Service and makes them available to the Inventory Service.

The application services do not communicate directly with one another.

Instead, they communicate through the `orders.created` destination.

```text
Order Service
      │
      ▼
RabbitMQ
      │
      ▼
orders.created
      │
      ▼
Inventory Service
```

The RabbitMQ binder handles the underlying broker interaction.

The application code uses Spring Cloud Stream APIs rather than directly managing RabbitMQ connections, exchanges, queues, and message delivery.

---

## Example Architecture

The example contains three Maven modules:

```text
messaging/
│
├── msg-order-service/
│
├── msg-inventory-service/
│
├── msg-integration-tests/
│
└── compose.yaml
```

### Order Service

The Order Service accepts incoming order requests.

After validating a request, it creates an `OrderCreatedEvent` and publishes it to RabbitMQ.

It does not directly call the Inventory Service.

### Inventory Service

The Inventory Service consumes `OrderCreatedEvent` messages.

When an event is received, it processes the order and reduces the available inventory for the corresponding product.

It also exposes an endpoint for querying inventory.

### Integration Tests

The integration-test module starts both services and a RabbitMQ container.

It verifies that an order submitted to the Order Service eventually results in an inventory update.

This demonstrates communication between independently configured Spring Boot application contexts through a real RabbitMQ broker.

---

## The Order-Created Event

The event represents something that has happened in the application: an order has been created.

The Order Service publishes this event after accepting an order.

The event contains:

```java
public record OrderCreatedEvent(String eventId, String orderId, String productId, int quantity, Instant occurredAt) {
}
```

The fields represent:

| Field        | Description                        |
|--------------|------------------------------------|
| `eventId`    | Unique identifier for the event.   |
| `orderId`    | Identifier of the created order.   |
| `productId`  | Identifier of the ordered product. |
| `quantity`   | Number of units ordered.           |
| `occurredAt` | Time when the event was created.   |

The `eventId` is generated using a UUID, while `occurredAt` records the event creation time.

### Why Use an Event?

An event communicates that something has already happened.

In this example:

```text
OrderCreatedEvent
```

means that an order has been created.

The Inventory Service reacts to this event without the Order Service needing to know how inventory processing is implemented.

This is an example of event-driven communication.

### Independent Event Definitions

The Order Service and Inventory Service each define their own `OrderCreatedEvent` record.

Although the Java records have the same structure, they belong to separate application modules.

```text
Order Service
    │
    └── OrderCreatedEvent

Inventory Service
    │
    └── OrderCreatedEvent
```

The services share a message contract through the serialized JSON structure rather than depending on a shared Java class.

This avoids introducing a compile-time dependency between the two services.

In a larger system, event schemas should be managed carefully to maintain compatibility as services evolve.

---

## Publishing Messages with StreamBridge

Spring Cloud Stream provides `StreamBridge` for sending messages to an output binding.

The Order Service uses it to publish `OrderCreatedEvent` messages.

The publisher is implemented as:

```java
@Component
public class OrderEventPublisher {

    private static final String OUTPUT_BINDING = "orderCreated-out-0";

    private final StreamBridge streamBridge;

    public OrderEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public boolean publish(OrderCreatedEvent event) {
        return streamBridge.send(OUTPUT_BINDING, event);
    }
}
```

### Understanding StreamBridge

`StreamBridge` allows application code to send messages to an output binding without declaring a dedicated functional producer.

The following statement publishes the event:

```java
streamBridge.send(OUTPUT_BINDING, event);
```

The arguments are:

- `OUTPUT_BINDING`: the name of the output binding.
- `event`: the object that will be serialized and sent as a message.

The output binding is configured as `orderCreated-out-0`.

The RabbitMQ binder uses the configured destination to determine where the message should be published.

### Why Use StreamBridge?

`StreamBridge` is useful when messages need to be published imperatively from application services.

For example, an order service may need to publish an event after completing a business operation.

It allows messaging to be integrated into ordinary application service methods.

### Understanding the Return Value

The `send` method returns a boolean indicating whether the message was accepted by the output binding.

A successful return does not mean that the Inventory Service has already processed the event.

It indicates that the message was successfully sent through the binding.

This distinction is important when working with asynchronous communication.

---

## Creating and Publishing an Order Event

The Order Service creates the event after receiving a valid order request.

Conceptually, the operation looks like:

```text
Order Request
      │
      ▼
Order Service
      │
      ▼
Create OrderCreatedEvent
      │
      ▼
Generate Event ID
      │
      ▼
Set Event Timestamp
      │
      ▼
Publish Event
      │
      ▼
Return Accepted Response
```

The service publishes the event using the publisher:

```java
orderEventPublisher.publish(event);
```

If publishing fails, the service throws an `IllegalStateException`.

Otherwise, it returns an `OrderAcceptedResponse`.

The response indicates that the order request was accepted for asynchronous processing.

---

## HTTP Response and Asynchronous Processing

The Order Service exposes:

```text
POST /api/orders
```

It accepts an order request containing:

```json
{
  "orderId": "ORD-1001",
  "productId": "SKU-1001",
  "quantity": 5
}
```

The request is validated before the event is created and published.

A successful request returns:

```http
HTTP/1.1 202 Accepted
```

With a response body similar to:

```json
{
  "orderId": "ORD-1001",
  "status": "ACCEPTED"
}
```

### Why HTTP 202 Accepted?

HTTP `202 Accepted` indicates that the request has been accepted for processing but processing has not necessarily completed.

In this example, the Order Service does not wait for the Inventory Service to consume the event.

Therefore, the response does not guarantee that inventory has already been updated.

The distinction is:

```text
HTTP 202 Accepted
        │
        ▼
Message Accepted by Output Binding
        │
        ▼
Order Service Returns Response
        │
        ▼
Inventory Service Processes Event Later
```

This is an important characteristic of asynchronous processing.

---

## Consuming Messages with the Functional Programming Model

Spring Cloud Stream supports a functional programming model based on Java functional interfaces.

Common functional interfaces include:

| Interface        | Purpose                                       |
|------------------|-----------------------------------------------|
| `Supplier<T>`    | Produces messages.                            |
| `Function<T, R>` | Processes an input and produces an output.    |
| `Consumer<T>`    | Consumes messages without returning a result. |

This example uses `Consumer<OrderCreatedEvent>` because the Inventory Service receives an event and performs an operation without producing another message.

The consumer is defined as:

```java
@Configuration
public class OrderEventConsumer {

    @Bean
    public Consumer<OrderCreatedEvent> orderCreated(InventoryService inventoryService) {
        return inventoryService::processOrder;
    }
}
```

### Understanding the Consumer

The method declares a Spring bean of type:

```java
Consumer<OrderCreatedEvent>
```

Spring Cloud Stream discovers the function and connects it to an input binding.

The function name is:

```text
orderCreated
```

The corresponding default input binding name is:

```text
orderCreated-in-0
```

When a message arrives, Spring Cloud Stream converts the message payload into an `OrderCreatedEvent` and invokes the consumer.

The method reference:

```java
inventoryService::processOrder
```

delegates event processing to the inventory service.

This keeps message consumption separate from business logic.

---

## Functional Binding Names

Spring Cloud Stream derives binding names from the function name.

For a consumer named `orderCreated`, the input binding is:

```text
orderCreated-in-0
```

For an output binding used with `StreamBridge`, the example defines:

```text
orderCreated-out-0
```

The naming convention is:

```text
<function-name>-in-0
<function-name>-out-0
```

The `0` represents the binding index.

This convention allows Spring Cloud Stream to associate functional beans with their corresponding message destinations.

---

## Configuring Message Bindings

Spring Cloud Stream uses configuration properties to connect application bindings to messaging destinations.

### Order Service Configuration

The Order Service defines its output binding:

```yaml
spring:
  cloud:
    stream:
      bindings:
        orderCreated-out-0:
          destination: orders.created
          content-type: application/json
```

The configuration means:

- `orderCreated-out-0` is the output binding.
- `orders.created` is the destination.
- `application/json` is the message content type.

When the Order Service publishes an event through `StreamBridge`, the RabbitMQ binder routes it to the configured destination.

### Inventory Service Configuration

The Inventory Service defines its function and input binding:

```yaml
spring:
  cloud:
    function:
      definition: orderCreated

    stream:
      bindings:
        orderCreated-in-0:
          destination: orders.created
          group: inventory-service
          content-type: application/json
```

The configuration means:

- `orderCreated` is the function to activate.
- `orderCreated-in-0` is its input binding.
- `orders.created` is the destination from which messages are consumed.
- `inventory-service` is the consumer group.
- `application/json` is the message content type.

### Matching Destinations

Both services must use the same destination.

| Service           | Binding              | Destination      |
|-------------------|----------------------|------------------|
| Order Service     | `orderCreated-out-0` | `orders.created` |
| Inventory Service | `orderCreated-in-0`  | `orders.created` |

The matching destination allows messages published by the Order Service to be consumed by the Inventory Service.

---

## Understanding Consumer Groups

A consumer group identifies a logical group of consumers that share responsibility for processing messages from a destination.

The Inventory Service uses:

```yaml
group: inventory-service
```

This creates a named consumer group.

With RabbitMQ, the binder uses the group to configure the corresponding queue subscription.

A named group provides durable subscription behavior, subject to the binder's configuration and broker availability.

### Consumer Group vs Anonymous Consumer

An anonymous consumer does not have a stable, explicitly configured group identity.

A named consumer group is useful when an application needs to receive messages as a persistent logical subscriber.

For example:

```text
orders.created
       │
       ▼
inventory-service
       │
       ▼
Inventory Service
```

If multiple instances of the Inventory Service use the same group, they can share the workload rather than each receiving an independent copy of every message.

Different consumer groups can independently receive messages from the same destination.

For example:

```text
                    orders.created
                           │
              ┌────────────┴────────────┐
              │                         │
              ▼                         ▼
      inventory-service          notification-service
              │                         │
              ▼                         ▼
       Update Inventory          Send Notification
```

Each group represents an independent subscription.

---

## JSON Serialization and Deserialization

The Order Service and Inventory Service use separate Java definitions of `OrderCreatedEvent`.

Spring Cloud Stream handles message conversion between the Java objects and the JSON message payload.

The publishing service sends an event object:

```java
streamBridge.send("orderCreated-out-0", event);
```

The message is serialized into JSON before being sent through the configured binding.

Conceptually:

```text
OrderCreatedEvent
       │
       ▼
JSON Serialization
       │
       ▼
RabbitMQ Message
```

The Inventory Service receives the message and converts the JSON payload into its own `OrderCreatedEvent` record.

```text
RabbitMQ Message
       │
       ▼
JSON Deserialization
       │
       ▼
Inventory OrderCreatedEvent
       │
       ▼
OrderEventConsumer
```

The two records do not need to share the same Java package or module.

They need compatible message structures.

Using JSON makes the event contract independent of the Java implementation details of either service.

---

## Processing an Order Event

The Inventory Service handles incoming events through `InventoryService`.

The service maintains an in-memory inventory:

```java
private final Map<String, Integer> inventory = new ConcurrentHashMap<>(Map.of("SKU-1001", 25, "SKU-2002", 10, "SKU-3003", 5));
```

When an order event is received, the available quantity is reduced:

```java
public void processOrder(OrderCreatedEvent event) {
    inventory.computeIfPresent(event.productId(), (productId, availableQuantity) -> 
            Math.max(0, availableQuantity - event.quantity()));
}
```

### Understanding the Inventory Update

The service uses `computeIfPresent` to update inventory only when the product exists.

The calculation is:

```text
New Available Quantity = Current Available Quantity - Ordered Quantity
```

The `Math.max(0, ...)` operation prevents the inventory quantity from becoming negative.

For example:

```text
Initial Inventory: 25
Ordered Quantity:   5
---------------------
Remaining Inventory: 20
```

If the product does not exist, the inventory remains unchanged.

### Why Use ConcurrentHashMap?

The example uses `ConcurrentHashMap` because message processing may occur on a different thread from HTTP request handling.

It provides thread-safe operations for individual map entries.

However, it does not provide durable storage or transactional guarantees across multiple operations.

The in-memory inventory is intentionally used to keep the example focused on messaging.

A production inventory service would normally use a persistent database and appropriate transaction and concurrency controls.

---

## Querying Inventory

The Inventory Service also exposes an endpoint for retrieving the available quantity of a product.

```text
GET /api/inventory/{productId}
```

For example:

```http
GET /api/inventory/SKU-1001
```

A successful response looks like:

```json
{
  "productId": "SKU-1001",
  "availableQuantity": 20
}
```

The endpoint allows the integration test to verify that the event has been processed.

It also demonstrates that the Inventory Service can expose an HTTP API while independently consuming messages.

---

## Complete Message Flow

The complete example can be represented as follows:

```text
                    HTTP Request
                         │
                         ▼
                  ┌──────────────┐
                  │ Order Service│
                  └──────────────┘
                         │
                         ▼
                 Validate Order Request
                         │
                         ▼
                 Create OrderCreatedEvent
                         │
                         ▼
                  StreamBridge.send()
                         │
                         ▼
                  ┌──────────────┐
                  │   RabbitMQ   │
                  └──────────────┘
                         │
                         ▼
                  orders.created
                         │
                         ▼
                orderCreated-in-0
                         │
                         ▼
                  ┌────────────────┐
                  │InventoryService│
                  └────────────────┘
                         │
                         ▼
                  processOrder()
                         │
                         ▼
                  Update Inventory
                         │
                         ▼
                 Inventory Updated
```

The Order Service returns its HTTP response after successfully sending the event through the output binding.

The Inventory Service processes the event independently.

---

## Running RabbitMQ

The example includes a Docker Compose configuration for running RabbitMQ locally.

The configuration is defined in:

```text
compose.yaml
```

It uses the RabbitMQ management image:

```yaml
services:
  rabbitmq:
    image: rabbitmq:4-management
    container_name: spring-cloud-messaging-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
```

### Starting RabbitMQ

From the messaging module directory:

```bash
cd 09-spring-cloud/messaging
docker compose up -d
```

RabbitMQ exposes two ports:

| Port    | Purpose                        |
|---------|--------------------------------|
| `5672`  | AMQP messaging protocol.       |
| `15672` | RabbitMQ management interface. |

The management interface is available at:

[http://localhost:15672](http://localhost:15672)

The example uses the following local credentials:

```text
Username: guest
Password: guest
```

### Stopping RabbitMQ

Run:

```bash
docker compose down
```

---

## Running the Applications

Start RabbitMQ before starting either application.

### Start the Inventory Service

From the repository root:

```bash
mvn -pl 09-spring-cloud/messaging/msg-inventory-service spring-boot:run
```

The Inventory Service starts on:

```text
http://localhost:8082
```

### Start the Order Service

In another terminal:

```bash
mvn -pl 09-spring-cloud/messaging/msg-order-service spring-boot:run
```

The Order Service starts on:

```text
http://localhost:8081
```

Both services connect to RabbitMQ using the default local configuration.

### Submit an Order

Send a request to the Order Service:

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-1001",
    "productId": "SKU-1001",
    "quantity": 5
  }'
```

The Order Service returns an accepted response.

### Check Inventory

After the Inventory Service has consumed the event, query:

```bash
curl http://localhost:8082/api/inventory/SKU-1001
```

The expected inventory quantity is:

```json
{
  "productId": "SKU-1001",
  "availableQuantity": 20
}
```

Because processing is asynchronous, the inventory query may initially return the previous quantity.

The update becomes visible after the Inventory Service processes the event.

---

## Testing Message-Driven Communication

The example uses Spring Boot testing support, JUnit, Testcontainers, and Awaitility.

Testing is divided into unit tests, MVC tests, and integration tests.

### Unit Tests

The unit tests verify application behavior independently of RabbitMQ.

#### InventoryServiceTest

The tests verify:

- Finding an existing inventory item.
- Returning an empty result for an unknown product.
- Reducing inventory when an order is processed.
- Preventing inventory from becoming negative.
- Leaving inventory unchanged for an unknown product.

#### OrderEventPublisherTest

The tests verify:

- Publishing an event successfully.
- Handling a failed message publication.

The tests mock `StreamBridge` to isolate the publisher from the messaging infrastructure.

#### OrderServiceTest

The tests verify:

- Creating an order and publishing its event.
- Throwing an exception when event publication fails.

These tests focus on the application's business behavior and publishing responsibilities.

### MVC Tests

The example uses `@WebMvcTest` and `MockMvc` to test HTTP endpoints.

#### OrderControllerTest

The tests verify:

- Accepting a valid order request.
- Rejecting a blank order identifier.
- Rejecting a blank product identifier.
- Rejecting a non-positive quantity.

#### InventoryControllerTest

The tests verify:

- Returning an existing inventory item.
- Returning HTTP 404 when a product does not exist.

These tests isolate the web layer from the messaging infrastructure.

---

## Integration Testing with Testcontainers

Unit tests are useful for verifying individual components, but they do not prove that messages can travel between independently running application contexts through a real broker.

The example uses Testcontainers to start a real RabbitMQ instance for integration testing.

The integration test:

1. Starts a RabbitMQ container.
2. Starts the Inventory Service application context.
3. Starts the Order Service application context.
4. Configures both services to connect to the container.
5. Submits an order through the Order Service HTTP endpoint.
6. Waits for the Inventory Service to consume the event.
7. Queries inventory through the Inventory Service HTTP endpoint.
8. Verifies that the inventory quantity has changed.

The architecture during the test looks like:

```text
             MessagingIntegrationTest
                       │
             ┌─────────┴─────────┐
             │                   │
             ▼                   ▼
       Order Service       Inventory Service
             │                   ▲
             │                   │
             ▼                   │
          RabbitMQ ──────────────┘
        Testcontainers
```

### RabbitMQContainer

The integration test uses `RabbitMQContainer` to manage the RabbitMQ lifecycle.

Conceptually:

```java
@Container
private static final RabbitMQContainer RABBITMQ =
        new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management")
        ).waitingFor(Wait.forLogMessage(".*Server startup complete.*", 1));
```

The container is started before the integration test executes and stopped after the test class finishes.

The log-based wait strategy waits for RabbitMQ's startup-complete message instead of relying only on a listening-port check.

This makes the test startup more reliable when the broker takes additional time to initialize.

### Independent Application Contexts

The integration test starts the two Spring Boot applications independently using `SpringApplicationBuilder`.

Each application receives its own server port and RabbitMQ connection properties.

The test explicitly configures the application names and messaging bindings.

The Inventory Service receives:

```text
spring.cloud.function.definition=orderCreated
spring.cloud.stream.bindings.orderCreated-in-0.destination=orders.created
spring.cloud.stream.bindings.orderCreated-in-0.group=inventory-service
```

The Order Service receives:

```text
spring.cloud.stream.bindings.orderCreated-out-0.destination=orders.created
```

Both applications therefore communicate through the same RabbitMQ destination.

Explicit configuration is particularly useful when multiple Spring Boot applications with separate `application.yml` files are started in the same test JVM.

### Testing Asynchronous Processing with Awaitility

An important characteristic of asynchronous processing is that the consumer may not finish before the publisher returns its HTTP response.

A test should therefore not immediately assume that the inventory has already been updated.

The example uses Awaitility to wait until the expected state becomes available.

Conceptually:

```java
await()
        .atMost(Duration.ofSeconds(20))
        .untilAsserted(() -> {
            InventoryItem inventoryItem = inventoryRestClient.get()
                    .uri("/api/inventory/{productId}", PRODUCT_ID)
                    .retrieve()
                    .body(InventoryItem.class);

            assertThat(inventoryItem).isNotNull();
            assertThat(inventoryItem.availableQuantity()).isEqualTo(20);
        });
```

This verifies the eventual result without introducing arbitrary thread sleeps.

The test waits for the asynchronous operation to complete within the specified timeout.

### What the Integration Test Proves

The integration test verifies the complete message flow:

```text
HTTP Request
     │
     ▼
Order Service
     │
     ▼
StreamBridge
     │
     ▼
RabbitMQ
     │
     ▼
Inventory Service Consumer
     │
     ▼
Inventory Update
     │
     ▼
HTTP Inventory Query
     │
     ▼
Assertion
```

Unlike a unit test that mocks `StreamBridge`, this test uses a real RabbitMQ broker.

It therefore verifies that the two services can communicate through the configured messaging infrastructure.

---

## Running the Tests

Run the integration tests from the repository root:

```bash
mvn -pl 09-spring-cloud/messaging/msg-integration-tests -am clean verify
```

To run only the messaging integration test:

```bash
mvn -pl 09-spring-cloud/messaging/msg-integration-tests -am \
    -Dtest=MessagingIntegrationTest \
    -Dsurefire.failIfNoSpecifiedTests=false \
    test
```

The integration tests require Docker to be available because Testcontainers starts a RabbitMQ container.

To run the tests across the complete messaging module:

```bash
mvn -pl 09-spring-cloud/messaging -am clean verify
```

---

## Important Messaging Considerations

Message-driven communication introduces additional considerations that are less prominent in simple synchronous HTTP communication.

### Message Delivery Is Asynchronous

A successful publishing operation does not mean the consumer has finished processing the message.

The producer and consumer operate independently.

Applications should distinguish between:

- Message publication.
- Message delivery.
- Message consumption.
- Successful business processing.

### Message Delivery and Retries

Message brokers and messaging binders provide mechanisms for handling delivery failures.

For example, a consumer may temporarily fail because a downstream dependency is unavailable.

Production applications should define appropriate retry and error-handling policies.

This introductory example does not implement custom retry policies or dead-letter queues.

### Duplicate Message Processing

Message-driven systems should account for the possibility that a message may be delivered or processed more than once, depending on the broker, acknowledgement behavior, retry configuration, and failure conditions.

A production consumer should consider idempotency.

For example, it could record processed event identifiers and avoid applying the same event more than once.

The example includes an `eventId` in the event structure, but does not implement persistent duplicate-event detection.

### Message Ordering

Message ordering can be important when multiple events affect the same business entity.

For example, two order events for the same product may need to be processed in a predictable order.

The ordering guarantees depend on the broker, destination configuration, partitioning or queue arrangement, and consumer concurrency.

Applications should not assume global ordering without explicitly designing for it.

### In-Memory Inventory

The Inventory Service stores its inventory in a `ConcurrentHashMap`.

This is suitable for demonstrating message consumption and state changes, but it is not persistent.

Inventory changes are lost when the application restarts.

A production implementation would normally use a database and appropriate transaction management.

---

## Message-Driven Communication vs Direct HTTP Calls

The two approaches solve different communication requirements.

| Aspect           | Direct HTTP                                                    | Message-Driven                                         |
|------------------|----------------------------------------------------------------|--------------------------------------------------------|
| Communication    | Request/response                                               | Publish/consume                                        |
| Coupling         | Caller knows the target service                                | Producer knows the destination                         |
| Response         | Usually waits for a response                                   | Does not wait for consumer processing                  |
| Availability     | Target service generally needs to be available during the call | Broker can decouple producer and consumer availability |
| Processing       | Usually synchronous                                            | Usually asynchronous                                   |
| Failure handling | HTTP errors and timeouts                                       | Messaging retries and error handling                   |
| Typical use      | Immediate responses and queries                                | Events and background processing                       |

Neither approach replaces the other.

HTTP is appropriate when an immediate response is required.

Messaging is useful when services need to communicate asynchronously and independently.

Many microservices architectures use both approaches.

---

## Key Takeaways

- Message-driven communication allows services to communicate through messages instead of direct service-to-service calls.
- Spring Cloud Stream provides an abstraction over messaging systems such as RabbitMQ and Kafka.
- A message binder connects Spring Cloud Stream to the underlying messaging infrastructure.
- RabbitMQ acts as the message broker in this example.
- `StreamBridge` allows application code to publish messages to output bindings.
- The functional programming model supports message production and consumption using `Supplier`, `Function`, and `Consumer`.
- `Consumer<T>` is appropriate when a message needs to be processed without producing another message.
- Spring Cloud Stream derives functional binding names from function names.
- Producers and consumers must use matching destinations to exchange messages.
- Consumer groups identify logical subscribers and allow multiple instances to share message processing.
- JSON serialization allows services to exchange event data without sharing Java classes.
- HTTP `202 Accepted` indicates that a request has been accepted, not that asynchronous processing has completed.
- Testcontainers allows integration tests to use a real RabbitMQ broker.
- Awaitility helps verify asynchronous processing without relying on arbitrary thread sleeps.
- Production messaging systems should consider retries, dead-letter queues, idempotency, message ordering, and persistent storage.

---

## Next Example

The next example will explore **Cloud-native Configuration** in Spring Cloud, demonstrating externalized configuration, centralized configuration management, and environment-specific configuration for distributed applications.
