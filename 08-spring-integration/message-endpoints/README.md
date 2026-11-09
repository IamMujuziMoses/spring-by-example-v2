# Message Endpoints

This example demonstrates **message endpoints** in Spring Integration.

It focuses on how Spring Integration connects application code to messaging infrastructure, how message endpoints consume messages from channels, and how endpoints delegate message processing to application services.

The example uses `@MessageEndpoint` and `@ServiceActivator` to create a message-consuming endpoint.

## What This Example Demonstrates

- What message endpoints are in Spring Integration.
- The relationship between message channels and message endpoints.
- The `@MessageEndpoint` annotation.
- The `@ServiceActivator` annotation.
- Receiving messages from a `MessageChannel`.
- Extracting message payloads for application processing.
- Separating messaging infrastructure from business logic.
- Delegating message processing to a Spring-managed service.
- Testing message endpoint behavior with Spring Boot integration tests.

---

## What Is a Message Endpoint?

A message endpoint is a component that connects application code to Spring Integration's messaging infrastructure.

It provides a boundary between the messaging system and the application's business logic.

Instead of requiring application services to manage message channels, subscriptions, and message delivery themselves, Spring Integration allows endpoints to handle the messaging concerns.

A simplified flow looks like:

```text
Message Publisher
       │
       ▼
Message Channel
       │
       ▼
Message Endpoint
       │
       ▼
Application Service
       │
       ▼
Business Processing
```

The publisher sends a message to a channel.

The endpoint receives the message and invokes the appropriate application logic.

This separation allows application services to remain independent of Spring Integration's messaging infrastructure.

---

## Message Channels and Message Endpoints

Message channels and message endpoints have different responsibilities.

### Message Channel

A message channel transports messages between components.

It provides the communication mechanism through which messages are sent and received.

For example:

```java
@Bean
public MessageChannel orderEndpointChannel() {
    return new DirectChannel();
}
```

This creates a `DirectChannel` named `orderEndpointChannel`.

### Message Endpoint

A message endpoint connects a message channel to application logic.

It receives messages from a configured input channel and delegates their processing.

In this example, the endpoint receives messages from `orderEndpointChannel` and invokes `OrderMessageService`.

Conceptually:

```text
Message Channel
      │
      ▼
Message Endpoint
      │
      ▼
Application Service
```

The channel handles message transport, while the endpoint handles the integration between messaging and application code.

---

## Message Endpoint Configuration

The example explicitly enables Spring Integration:

```java
@Configuration
@EnableIntegration
public class MessageEndpointConfiguration {

    @Bean
    public MessageChannel orderEndpointChannel() {
        return new DirectChannel();
    }
}
```

`@EnableIntegration` enables Spring Integration infrastructure and its annotation-based configuration support.

The `orderEndpointChannel` bean provides the channel used to deliver messages to the endpoint.

The example uses a `DirectChannel`, which provides synchronous message delivery.

This means that the message is dispatched to its subscriber in the sender's thread.

---

## The `@MessageEndpoint` Annotation

Spring Integration provides `@MessageEndpoint` to identify a class as a message endpoint.

For example:

```java
@MessageEndpoint
public class OrderMessageEndpoint {

    // Endpoint implementation
}
```

`@MessageEndpoint` is a Spring component stereotype, so the class is registered as a Spring-managed component.

It identifies the class as part of the messaging infrastructure.

The methods inside the endpoint can then use Spring Integration annotations to define how messages are handled.

---

## The `@ServiceActivator` Annotation

The example uses `@ServiceActivator` to configure a method as a message-consuming endpoint.

```java
@MessageEndpoint
public class OrderMessageEndpoint {

    private final OrderMessageService orderMessageService;

    public OrderMessageEndpoint(OrderMessageService orderMessageService) {
        this.orderMessageService = orderMessageService;
    }

    @ServiceActivator(inputChannel = "orderEndpointChannel")
    public void handleOrder(OrderMessage order) {
        orderMessageService.processOrder(order);
    }
}
```

The `@ServiceActivator` annotation connects the `handleOrder` method to the `orderEndpointChannel`.

When a message arrives on that channel, Spring Integration invokes the method.

The method receives the message payload as an `OrderMessage` object.

The endpoint then delegates processing to `OrderMessageService`.

In this example, `@ServiceActivator` is used to demonstrate how a message endpoint connects messaging infrastructure to application logic. More advanced service activator behavior is covered separately.

---

## Message Payload Handling

Spring Integration messages contain a payload and headers.

A simplified representation is:

```text
Message
  │
  ├── Payload
  │     └── OrderMessage
  │
  └── Headers
        ├── Message ID
        ├── Timestamp
        └── Other Metadata
```

The payload contains the actual application data.

The headers contain metadata associated with the message.

This example uses the following domain record:

```java
public record OrderMessage(long orderId, String description) {}
```

The publisher creates a Spring Integration message containing this object:

```java
MessageBuilder.withPayload(order).build()
```

The endpoint method declares an `OrderMessage` parameter:

```java
public void handleOrder(OrderMessage order)
```

Spring Integration resolves the method argument from the message payload.

The endpoint therefore does not need to manually extract the payload using `Message.getPayload()`.

This keeps the endpoint implementation simple and focused on delegating the message to the application service.

---

## Publishing Messages to the Endpoint

The example uses `OrderMessagePublisher` to send messages to the configured channel.

```java
@Service
public class OrderMessagePublisher {

    private final MessageChannel orderEndpointChannel;

    public OrderMessagePublisher(@Qualifier("orderEndpointChannel") MessageChannel orderEndpointChannel) {
        this.orderEndpointChannel = orderEndpointChannel;
    }

    public boolean publishOrder(OrderMessage order) {
        return orderEndpointChannel.send(MessageBuilder.withPayload(order).build());
    }
}
```

The publisher receives the `orderEndpointChannel` through constructor injection.

The `publishOrder` method:

1. Receives an `OrderMessage`.
2. Creates a Spring Integration message using `MessageBuilder`.
3. Sends the message to `orderEndpointChannel`.
4. Returns the result of the channel's `send` operation.

The publisher does not need to know which endpoint subscribes to the channel or how the message is processed.

This keeps the publisher independent of the endpoint implementation.

---

## Separating Messaging from Business Logic

One of the main purposes of message endpoints is to separate messaging concerns from application logic.

The example uses `OrderMessageService` to represent the business processing layer.

```java
@Service
public class OrderMessageService {

    private final MessageEndpointSupport support;

    public OrderMessageService(MessageEndpointSupport support) {
        this.support = support;
    }

    public void processOrder(OrderMessage order) {
        support.recordProcessedOrder(order);
    }
}
```

Notice that the service receives an `OrderMessage` directly.

It does not depend on:

- `MessageChannel`.
- `MessageHandler`.
- `Message`.
- `MessageBuilder`.
- Spring Integration endpoint annotations.

The service focuses on processing the application data.

The endpoint is responsible for connecting the messaging infrastructure to that service.

This separation makes the business logic easier to reuse and test independently of the messaging implementation.

---

## Message Processing Support

The example uses `MessageEndpointSupport` to record processed messages.

```java
@Component
public class MessageEndpointSupport {

    private final List<OrderMessage> processedOrders = new CopyOnWriteArrayList<>();

    public void recordProcessedOrder(OrderMessage order) {
        processedOrders.add(order);
    }

    public List<OrderMessage> processedOrders() {
        return List.copyOf(processedOrders);
    }

    public void reset() {
        processedOrders.clear();
    }
}
```

This support component provides a simple way to observe the results of message processing.

It uses a `CopyOnWriteArrayList` to store processed messages.

The `processedOrders()` method returns an immutable snapshot of the recorded messages.

The `reset()` method clears the recorded messages between tests.

This is example support code rather than a production persistence implementation.

---

## Message Endpoint Flow

The complete message processing flow looks like:

```text
OrderMessagePublisher
         │
         │ publishOrder()
         ▼
MessageBuilder
         │
         │ Creates Message<OrderMessage>
         ▼
orderEndpointChannel
         │
         │ DirectChannel
         ▼
OrderMessageEndpoint
         │
         │ @ServiceActivator
         ▼
handleOrder(OrderMessage)
         │
         ▼
OrderMessageService
         │
         │ processOrder()
         ▼
MessageEndpointSupport
         │
         ▼
Processed Order Recorded
```

Each component has a distinct responsibility:

| Component                | Responsibility                                                 |
|--------------------------|----------------------------------------------------------------|
| `OrderMessagePublisher`  | Creates and sends messages.                                    |
| `orderEndpointChannel`   | Transports messages to subscribers.                            |
| `OrderMessageEndpoint`   | Receives messages and connects messaging to application logic. |
| `OrderMessageService`    | Performs application-level processing.                         |
| `MessageEndpointSupport` | Records processed messages for the example and its tests.      |

---

## Why Use a Message Endpoint?

Without a message endpoint, application code would need to manage messaging infrastructure directly.

This could lead to business services becoming tightly coupled to message channels and message-handling APIs.

With a message endpoint, the responsibilities are separated:

```text
Messaging Infrastructure       Application Logic
------------------------       -----------------

MessageChannel
      │
      ▼
Message Endpoint  ───────────► OrderMessageService
      │
      ▼
Message Handling
```

The endpoint acts as an adapter between the messaging infrastructure and the application service.

This makes it easier to change messaging configurations without unnecessarily changing business logic.

---

## Synchronous Message Processing

This example uses `DirectChannel`.

By default, a `DirectChannel` invokes its subscriber in the sender's thread.

Consequently, message processing happens synchronously.

The flow is:

```text
Publisher Thread
       │
       ▼
Send Message
       │
       ▼
DirectChannel
       │
       ▼
Message Endpoint
       │
       ▼
Application Service
       │
       ▼
Processing Completes
       │
       ▼
Send Returns
```

Because processing occurs synchronously, the integration tests can inspect the processed messages immediately after publishing them.

This example does not introduce asynchronous processing or message queues.

---

## Understanding the `send()` Result

The publisher returns the result of:

```java
orderEndpointChannel.send(message);
```

The boolean result indicates whether the message was accepted and dispatched by the channel.

It should not be interpreted as proof that the business operation completed successfully in every possible messaging configuration.

In this example, the `DirectChannel` dispatches the message synchronously to the endpoint, making the processing flow straightforward to observe.

---

## Message Endpoints vs Message Handlers

Message endpoints and message handlers are related but different concepts.

A **message endpoint** connects application code to the messaging infrastructure.

A **message handler** is the component that performs the actual handling of a message.

In this example, Spring Integration uses the annotated `handleOrder` method as the message-handling method behind the endpoint.

Conceptually:

```text
Message Channel
      │
      ▼
Message Endpoint
      │
      ▼
Message Handler Method
      │
      ▼
Application Service
```

The next examples explore message handlers and other endpoint-related components in more detail.

---

## Running the Example

From the repository root:

```bash
mvn -pl 08-spring-integration/message-endpoints spring-boot:run
```

The application starts with the Spring Integration message channel and endpoint configured.

No external message broker is required because the example uses an in-process `DirectChannel`.

---

## Running the Tests

Run the tests from the module:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 08-spring-integration/message-endpoints test
```

To perform a clean verification:

```bash
mvn -pl 08-spring-integration/message-endpoints clean verify
```

---

## Key Takeaways

- Message endpoints connect application code to Spring Integration's messaging infrastructure.
- Message channels transport messages between components.
- `@MessageEndpoint` identifies a Spring-managed message endpoint.
- `@ServiceActivator` connects a method to an input message channel.
- Spring Integration can supply a message payload directly to a method parameter.
- Endpoints allow messaging concerns to remain separate from business logic.
- Application services do not need to depend on Spring Integration APIs.
- `DirectChannel` provides synchronous message delivery by default.
- The result of `MessageChannel.send()` indicates message acceptance and dispatch, not necessarily business-level success.
- Integration tests can verify the complete flow from message publication to application processing.

---

## Next Example

The next example will explore **Message Handlers** in Spring Integration, including how messages are handled programmatically and how message handlers interact with message channels.
