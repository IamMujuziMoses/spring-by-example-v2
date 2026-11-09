# Message Handlers

This example demonstrates **message handlers** in Spring Integration.

It focuses on how to implement a custom `MessageHandler`, subscribe it to a message channel, receive incoming messages, access message payloads and headers, and delegate processing to an application service.

## What This Example Demonstrates

- What message handlers are in Spring Integration.
- The purpose of Spring Messaging's `MessageHandler` interface.
- Implementing a custom message handler.
- Subscribing a message handler to a `DirectChannel`.
- Receiving and processing messages.
- Accessing message payloads and headers.
- Validating message payload types.
- Separating message handling from business logic.
- Publishing messages through a `MessageChannel`.
- Testing message handler behavior with Spring Boot integration tests.

---

## What Is a Message Handler?

A message handler is a component responsible for processing messages delivered through a messaging channel.

In Spring Integration, a message handler implements the `MessageHandler` interface.

```java
public interface MessageHandler {

    void handleMessage(Message<?> message);

}
```

The `handleMessage()` method receives a message and performs the required processing.

A handler can:

- Extract information from the message.
- Validate the message payload.
- Read message headers.
- Delegate processing to an application service.
- Transform or otherwise act on the message.

A simplified message processing flow looks like:

```text
Message Publisher
       │
       ▼
Message Channel
       │
       ▼
Message Handler
       │
       ▼
Application Service
       │
       ▼
Business Processing
```

The message channel delivers messages, while the handler determines what happens when a message is received.

---

## Spring Messaging Message

Spring Integration builds on Spring Messaging's message abstraction.

A message consists of two main parts:

1. **Payload** — the actual data being transmitted.
2. **Headers** — metadata associated with the message.

Conceptually:

```text
Message
  │
  ├── Payload
  │     └── OrderMessage
  │
  └── Headers
        ├── id
        ├── timestamp
        └── source
```

The payload represents the business data, while headers provide additional information about the message.

For example, an order message might contain:

```java
OrderMessage order = new OrderMessage(1L, "Order 1");

Message<OrderMessage> message = MessageBuilder.withPayload(order).setHeader("source", "web").build();
```

Here:

- The payload contains the order information.
- The `source` header identifies where the message originated.
- Spring Messaging automatically provides standard headers such as message ID and timestamp.

---

## Implementing a Custom Message Handler

A custom handler is created by implementing Spring Messaging's `MessageHandler` interface.

The example defines an `OrderMessageHandler`:

```java
@Component
public class OrderMessageHandler implements MessageHandler {

    private final OrderMessageService orderMessageService;

    public OrderMessageHandler(OrderMessageService orderMessageService) {
        this.orderMessageService = orderMessageService;
    }

    @Override
    public void handleMessage(Message<?> message) {
        if (!(message.getPayload() instanceof OrderMessage order)) {
            throw new IllegalArgumentException("Unsupported message payload");
        }

        MessageHeaders headers = message.getHeaders();
        String source = headers.get("source", String.class);

        orderMessageService.process(order, source);
    }

}
```

The handler implements `handleMessage()` and receives a `Message<?>`.

The `Message<?>` type allows the handler to receive messages with different payload types. The handler is therefore responsible for checking whether the payload is one it can process.

In this example, only `OrderMessage` payloads are accepted.

The handler also extracts the `source` header before delegating processing to `OrderMessageService`.

---

## Accessing the Message Payload

The payload is accessed using:

```java
message.getPayload()
```

The returned object represents the actual data carried by the message.

Since the handler expects an `OrderMessage`, it validates the payload type:

```java
if (!(message.getPayload() instanceof OrderMessage order)) {
    throw new IllegalArgumentException("Unsupported message payload");
}
```

This ensures that the handler processes only messages containing the expected payload.

The pattern matching syntax also makes the validated payload available through the `order` variable.

---

## Accessing Message Headers

Message headers contain metadata associated with a message.

They are accessed through:

```java
MessageHeaders headers = message.getHeaders();
```

The example retrieves the `source` header:

```java
String source = headers.get("source", String.class);
```

The publisher sets this header when constructing the message:

```java
Message<OrderMessage> message = MessageBuilder.withPayload(order).setHeader("source", source).build();
```

For example, the publisher can identify different message sources:

```text
Order Message
      │
      ├── source = web
      │
      ├── source = mobile
      │
      └── source = batch
```

The handler can use this metadata without requiring the source information to become part of the business payload.

The `source` header is optional in this example. If it is absent, the handler receives `null` for that value.

---

## Message Channels and Message Handlers

A message channel provides the communication path between a message publisher and one or more message consumers.

A message handler is a consumer that processes messages delivered through a channel.

This example uses `DirectChannel`.

```java
@Bean
public MessageChannel orderHandlerChannel(OrderMessageHandler orderMessageHandler) {
    DirectChannel channel = new DirectChannel();
    channel.subscribe(orderMessageHandler);

    return channel;
}
```

The handler is explicitly subscribed to the channel:

```java
channel.subscribe(orderMessageHandler);
```

This establishes the relationship between the channel and the handler.

When a message is sent to the channel, the subscribed handler receives it.

Conceptually:

```text
                 DirectChannel
                      │
                      ▼
              OrderMessageHandler
                      │
                      ▼
              OrderMessageService
```

---

## Why DirectChannel?

`DirectChannel` is a point-to-point channel that delivers messages to a subscribed handler.

It uses synchronous message delivery by default.

This means that the sender's thread executes the message handler before the `send()` operation completes.

For this example, synchronous delivery makes the processing flow straightforward:

```text
Publisher
    │
    ▼
DirectChannel
    │
    ▼
MessageHandler
    │
    ▼
OrderMessageService
    │
    ▼
Processing Complete
    │
    ▼
Publisher Continues
```

This also makes integration testing simpler because the processed message can be inspected immediately after publishing.

The example intentionally uses a `DirectChannel` to focus on message handler implementation rather than asynchronous processing.

---

## Registering the Message Handler

The handler is registered as a Spring-managed component:

```java
@Component
public class OrderMessageHandler implements MessageHandler {
    // ...
}
```

Spring discovers the component through component scanning and injects its dependencies.

The channel configuration then subscribes the handler to the `DirectChannel`:

```java
@Bean
public MessageChannel orderHandlerChannel(OrderMessageHandler orderMessageHandler) {
    DirectChannel channel = new DirectChannel();
    channel.subscribe(orderMessageHandler);

    return channel;
}
```

There are two distinct responsibilities here:

- Spring manages the handler as a bean.
- The channel subscription determines which handler receives messages sent to the channel.

Registering a handler as a Spring bean does not, by itself, subscribe it to an arbitrary `DirectChannel`.

---

## Publishing Messages

The example uses `OrderMessagePublisher` to publish messages.

```java
@Component
public class OrderMessagePublisher {

    private final MessageChannel orderHandlerChannel;

    public OrderMessagePublisher(@Qualifier("orderHandlerChannel") MessageChannel orderHandlerChannel) {
        this.orderHandlerChannel = orderHandlerChannel;
    }

    public boolean publish(OrderMessage order, String source) {
        Message<OrderMessage> message = MessageBuilder.withPayload(order).setHeader("source", source).build();

        return orderHandlerChannel.send(message);
    }

}
```

The publisher:

1. Receives an `OrderMessage`.
2. Creates a Spring Messaging message.
3. Adds the `source` header.
4. Sends the message to the configured channel.

The following example publishes an order:

```java
OrderMessage order = new OrderMessage(1L, "Order 1");

publisher.publish(order, "web");
```

The message is delivered to `OrderMessageHandler`, which processes it through the application service.

The `send()` method returns a boolean indicating whether the channel accepted the message for delivery. It should not be treated as a general guarantee of successful business processing.

---

## Separating Message Handling from Business Logic

A message handler should not necessarily contain all the application's business logic.

This example delegates processing to `OrderMessageService`:

```java
@Service
public class OrderMessageService {

    private final MessageHandlerSupport support;

    public OrderMessageService(MessageHandlerSupport support) {
        this.support = support;
    }

    public void process(OrderMessage order, String source) {
        support.recordProcessedOrder(order, source);
    }

}
```

This separation gives each component a clear responsibility.

| Component               | Responsibility                               |
|-------------------------|----------------------------------------------|
| `OrderMessagePublisher` | Creates and publishes messages.              |
| `DirectChannel`         | Delivers messages to subscribed handlers.    |
| `OrderMessageHandler`   | Extracts and validates message information.  |
| `OrderMessageService`   | Performs application-level processing.       |
| `MessageHandlerSupport` | Records processed messages for verification. |

Keeping business processing separate from message handling makes the application logic easier to reuse and test independently.

---

## Observing Message Processing

The example uses `MessageHandlerSupport` to record processed orders.

```java
@Component
public class MessageHandlerSupport {

    private final List<ProcessedOrder> processedOrders = new CopyOnWriteArrayList<>();

    public void recordProcessedOrder(OrderMessage order, String source) {
        processedOrders.add(new ProcessedOrder(order, source));
    }

    public List<ProcessedOrder> processedOrders() {
        return List.copyOf(processedOrders);
    }

    public void clear() {
        processedOrders.clear();
    }

    public record ProcessedOrder(OrderMessage order, String source) {
    }

}
```

This component provides a simple way to observe the results of message processing.

It uses `CopyOnWriteArrayList` for thread-safe access to the recorded messages and returns an immutable snapshot when queried.

It is demonstration and test support, not a production message store or persistence mechanism.

---

## Message Handler Processing Flow

A complete message processing flow in this example looks like:

```text
OrderMessagePublisher
        │
        ▼
Create Message
        │
        ├── Payload: OrderMessage
        │
        └── Header: source
        │
        ▼
orderHandlerChannel
        │
        ▼
DirectChannel
        │
        ▼
OrderMessageHandler
        │
        ├── Extract Payload
        │
        ├── Validate Payload Type
        │
        └── Read Message Headers
        │
        ▼
OrderMessageService
        │
        ▼
MessageHandlerSupport
        │
        ▼
Processed Order Recorded
```

---

## Why Use Integration Tests?

The purpose of this example is to demonstrate how Spring Integration components work together.

Testing only the `OrderMessageHandler` would not verify that:

- The channel is configured correctly.
- The handler is subscribed to the channel.
- The publisher sends messages to the intended channel.
- The complete message processing flow works.

The integration tests exercise the complete flow:

```text
Publisher
    │
    ▼
Channel
    │
    ▼
Handler
    │
    ▼
Service
    │
    ▼
Processing Result
```

Because `DirectChannel` delivers messages synchronously, the tests can inspect the processing results immediately after publishing.

---

## Message Handlers vs Message Endpoints

Spring Integration provides several ways to connect message consumers to channels.

This example uses a custom `MessageHandler` implementation with an explicit channel subscription.

The previous **Message Endpoints** example demonstrates a different approach using the `@ServiceActivator` annotation.

| Aspect             | Message Handlers              | Message Endpoints                        |
|--------------------|-------------------------------|------------------------------------------|
| Main focus         | Implementing a custom handler | Configuring a message-consuming endpoint |
| Handler definition | Implements `MessageHandler`   | Uses an annotated service method         |
| Configuration      | Explicit channel subscription | `@ServiceActivator` annotation           |
| Processing method  | `handleMessage()`             | Annotated method                         |
| Main abstraction   | Message handler               | Message endpoint                         |

Both approaches allow application code to process messages received through Spring Integration channels.

A message endpoint commonly adapts an application method or other message-handling component to the messaging infrastructure.

A custom `MessageHandler` provides direct control over the message-handling implementation.

---

## Running the Example

From the repository root:

```bash
mvn -pl 08-spring-integration/message-handlers spring-boot:run
```

The application starts with the message handler and channel configuration.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 08-spring-integration/message-handlers test
```

To run the complete verification lifecycle:

```bash
mvn -pl 08-spring-integration/message-handlers -am clean verify
```

---

## Key Takeaways

- A message handler processes messages delivered through a Spring Integration channel.
- Spring Messaging provides the `MessageHandler` interface.
- The `handleMessage()` method receives the complete message.
- A message consists of a payload and headers.
- `message.getPayload()` retrieves the message payload.
- `message.getHeaders()` provides access to message metadata.
- A custom handler can be subscribed directly to a `DirectChannel`.
- A `DirectChannel` provides synchronous, point-to-point message delivery by default.
- Message handling and business processing can be separated into different components.
- Integration tests can verify the complete publisher-to-handler processing flow.
- `@ServiceActivator` provides an alternative way to configure message-consuming endpoints.

---

## Next Example

The next example will explore **Message Transformers** in Spring Integration, including how to transform message payloads while preserving or modifying message headers.
