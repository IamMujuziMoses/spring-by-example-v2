# Filters

This example demonstrates **message filtering** in Spring Integration.

It focuses on how Spring Integration evaluates incoming messages against a condition, how accepted messages are forwarded to an output channel, and how rejected messages are prevented from continuing through an integration flow.

The example receives an `OrderMessage` and filters it based on its quantity. Only orders with a positive quantity are forwarded to the downstream message handler.

## What This Example Demonstrates

- What message filtering means in Spring Integration.
- The role of filters in message-driven applications.
- Spring Integration's `@Filter` annotation.
- Configuring input and output message channels.
- Accepting and rejecting messages based on payload conditions.
- Forwarding accepted messages to an output channel.
- Preventing rejected messages from reaching downstream handlers.
- Preserving message headers through a filter.
- Testing message filtering with Spring Boot test support.

---

## What Is Message Filtering?

Message filtering is the process of evaluating an incoming message against a condition to determine whether it should continue through an integration flow.

A filter examines a message and decides whether it satisfies a particular requirement.

If the message satisfies the condition, it is accepted and forwarded.

If the message does not satisfy the condition, it is rejected and is not forwarded to the output channel.

A simplified flow looks like:

```text
Incoming Message
       │
       ▼
     Filter
       │
       ▼
  Evaluate Condition
       │
       ├── Accepted ──► Output Channel
       │
       └── Rejected ──► Not Forwarded
```

This allows applications to control which messages are processed by downstream components.

For example, an order-processing application might only want to process orders with a positive quantity.

Instead of allowing every incoming order to reach the processing component, a filter can reject orders that do not satisfy this condition.

---

## Spring Integration Messages

Spring Integration uses Spring Messaging's `Message` abstraction to represent messages exchanged between components.

A message consists of two main parts:

- **Payload:** The actual data being transported.
- **Headers:** Metadata associated with the message.

Conceptually:

```text
Message
  │
  ├── Payload
  │     └── OrderMessage
  │
  └── Headers
        └── source = "web"
```

The payload contains the business data, while headers provide additional information about the message.

In this example, the filter evaluates the `OrderMessage` payload.

The message also contains a `source` header identifying where the order originated.

The filter determines whether the message should continue without needing to modify its payload.

---

## OrderMessage

The example uses an `OrderMessage` record to represent an incoming order.

```java
public record OrderMessage(long orderId, String product, int quantity) {
}
```

It contains:

- `orderId`: The order identifier.
- `product`: The product name.
- `quantity`: The ordered quantity.

For example:

```java
new OrderMessage(1L, "Laptop", 2);
```

This represents an order for two laptops.

The filter uses the `quantity` field to determine whether the order should be accepted.

---

## The `@Filter` Annotation

Spring Integration provides the `@Filter` annotation to identify a method that evaluates messages.

The example uses:

```java
@MessageEndpoint
public class OrderMessageFilter {

    @Filter(inputChannel = "orderInputChannel", outputChannel = "orderOutputChannel")
    public boolean accept(OrderMessage order) {
        return order.quantity() > 0;
    }

}
```

The `@MessageEndpoint` annotation identifies the class as a Spring Integration message endpoint.

The `@Filter` annotation identifies the method as a message filter and connects it to the integration flow.

It defines two channels:

- `inputChannel`: The channel from which incoming messages are received.
- `outputChannel`: The channel to which accepted messages are forwarded.

The method receives the incoming `OrderMessage` payload and evaluates its quantity.

It returns a boolean indicating whether the message should continue.

---

## How the Filter Works

The filter evaluates the following condition:

```java
return order.quantity() > 0;
```

This means an order is accepted only when its quantity is greater than zero.

### Accepted Message

Consider the following order:

```java
new OrderMessage(1L, "Laptop", 2);
```

The condition evaluates to:

```java
2 > 0
```

The result is `true`.

Spring Integration therefore forwards the original message to `orderOutputChannel`.

### Rejected Message

Consider another order:

```java
new OrderMessage(2L, "Keyboard", 0);
```

The condition evaluates to:

```java
0 > 0
```

The result is `false`.

Spring Integration rejects the message, and it is not forwarded to the output channel.

The filter does not need to throw an exception to reject a message.

---

## Understanding the Filter's Return Value

The return value of a filter method determines whether a message is accepted or rejected.

### `true`

```java
return true;
```

The message is accepted and forwarded to the configured output channel.

### `false`

```java
return false;
```

The message is rejected and is not forwarded to the configured output channel.

This is different from a transformer.

A transformer returns a new payload, while a filter returns a boolean indicating whether the original message should continue.

Conceptually:

```text
Transformer:

Incoming Message
       │
       ▼
   Transform
       │
       ▼
  New Payload
       │
       ▼
  Output Channel
```

```text
Filter:

Incoming Message
       │
       ▼
  Evaluate Condition
       │
       ├── true ──► Forward Original Message
       │
       └── false ─► Reject Message
```

---

## Input and Output Channels

The example uses two `DirectChannel` instances:

- `orderInputChannel`
- `orderOutputChannel`

They represent the entry and exit points of the filtering process.

The channels are configured in `FilterConfiguration`.

```java
@Configuration
@EnableIntegration
public class FilterConfiguration {

    @Bean
    public MessageChannel orderInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel orderOutputChannel(FilterSupport support) {
        DirectChannel channel = new DirectChannel();
        channel.subscribe(support);

        return channel;
    }

}
```

### Input Channel

The `orderInputChannel` receives incoming order messages.

The filter subscribes to this channel through its `@Filter` configuration.

```text
Publisher
    │
    ▼
orderInputChannel
    │
    ▼
OrderMessageFilter
```

### Output Channel

The `orderOutputChannel` receives messages accepted by the filter.

The example explicitly subscribes `FilterSupport` to this channel.

```text
OrderMessageFilter
        │
        ▼
orderOutputChannel
        │
        ▼
   FilterSupport
```

Rejected messages do not reach this channel.

---

## Why Use DirectChannel?

Spring Integration provides different channel implementations for different messaging requirements.

This example uses `DirectChannel`.

A `DirectChannel` delivers messages to its subscribed handler in the sender's thread.

Conceptually:

```text
Publisher Thread
       │
       ▼
DirectChannel
       │
       ▼
     Filter
       │
       ├── Accepted ──► Output DirectChannel
       │                       │
       │                       ▼
       │                 Downstream Handler
       │
       └── Rejected ──► Not Forwarded
```

The processing flow is synchronous.

This makes `DirectChannel` suitable for demonstrating message filtering without introducing asynchronous processing or external messaging infrastructure.

It also means the downstream handler can be inspected immediately after publishing an accepted message.

---

## Message Publisher

The `OrderMessagePublisher` is responsible for sending incoming orders to the input channel.

It injects the `orderInputChannel`:

```java
public OrderMessagePublisher(@Qualifier("orderInputChannel") MessageChannel orderInputChannel) {
    this.orderInputChannel = orderInputChannel;
}
```

It then creates and sends a message:

```java
public void publish(OrderMessage order, String source) {
    Message<OrderMessage> message = MessageBuilder.withPayload(order).setHeader("source", source).build();

    orderInputChannel.send(message);
}
```

The publisher adds a `source` header to identify where the order originated.

For example:

```java
publisher.publish(order, "web");
```

The resulting message contains:

```text
Payload:
  OrderMessage

Headers:
  source = "web"
```

The publisher does not determine whether an order is valid.

Its responsibility is to create and publish the message.

The filtering decision belongs to `OrderMessageFilter`.

---

## Downstream Message Handling

After a message passes the filter, it is forwarded to `orderOutputChannel`.

The example uses `FilterSupport` as the downstream message handler.

It implements Spring Messaging's `MessageHandler` interface:

```java
public class FilterSupport implements MessageHandler {

    @Override
    public void handleMessage(Message<?> message) {
        if (!(message.getPayload() instanceof OrderMessage order)) {
            throw new IllegalArgumentException("Unsupported message payload");
        }

        String source = message.getHeaders().get("source", String.class);

        processedOrders.add(new ProcessedOrder(order, source));
    }

}
```

The handler validates that the received payload is an `OrderMessage`.

It then reads the `source` header and records the accepted order.

This allows the example to demonstrate which messages successfully passed through the filter.

The handler does not need to evaluate the quantity again because that decision has already been made by the filter.

---

## What Happens to Rejected Messages?

By default, when a filter rejects a message, Spring Integration does not forward it to the configured output channel.

For example:

```java
return order.quantity() > 0;
```

When the quantity is zero, the filter returns `false`.

The message is rejected and does not reach `FilterSupport`.

Importantly, rejection does not automatically mean an exception is thrown.

The message simply does not continue through the normal output path.

Spring Integration also provides additional options for handling rejected messages.

### Discard Channel

The `discardChannel` attribute allows rejected messages to be sent to another channel.

For example:

```java
@Filter(inputChannel = "orderInputChannel", outputChannel = "orderOutputChannel", discardChannel = "rejectedOrderChannel")
public boolean accept(OrderMessage order) {
    return order.quantity() > 0;
}
```

Accepted messages continue to `orderOutputChannel`, while rejected messages are sent to `rejectedOrderChannel`.

Conceptually:

```text
Incoming Message
       │
       ▼
     Filter
       │
       ├── Accepted ──► orderOutputChannel
       │
       └── Rejected ──► rejectedOrderChannel
```

### Throw Exception on Rejection

The `throwExceptionOnRejection` attribute can be used when a rejected message should result in an exception.

For example:

```java
@Filter(inputChannel = "orderInputChannel", outputChannel = "orderOutputChannel", throwExceptionOnRejection = "true")
public boolean accept(OrderMessage order) {
    return order.quantity() > 0;
}
```

When the filter rejects a message, Spring Integration throws a `MessageRejectedException`.

These options are not enabled in this example because its primary purpose is to demonstrate basic accept-or-reject behavior.

---

## Message Headers During Filtering

A message contains both a payload and headers.

In this example, the publisher adds a `source` header:

```java
.setHeader("source", source)
```

The filter evaluates the payload without changing it.

When an accepted message is forwarded, its original message information, including its headers, remains available to the downstream handler.

For example:

```text
Incoming Message
  │
  ├── Payload: OrderMessage
  │
  └── Header: source = "web"
          │
          ▼
        Filter
          │
          ▼
     Accepted Message
  │
  ├── Payload: OrderMessage
  │
  └── Header: source = "web"
          │
          ▼
    Downstream Handler
```

The integration tests verify that the source information remains available when accepted messages reach the downstream handler.

---

## Filtering Flow

The complete integration flow looks like:

```text
OrderMessagePublisher
        │
        │ Message<OrderMessage>
        │ source = "web"
        ▼
  orderInputChannel
        │
        ▼
   OrderMessageFilter
        │
        │ Evaluate quantity > 0
        │
        ├── Accepted
        │       │
        │       ▼
        │  orderOutputChannel
        │       │
        │       ▼
        │  FilterSupport
        │       │
        │       ▼
        │  Record Processed Order
        │
        └── Rejected
                │
                ▼
          Not Forwarded
```

The responsibilities are separated:

| Component               | Responsibility                                       |
|-------------------------|------------------------------------------------------|
| `OrderMessagePublisher` | Creates and publishes incoming order messages.       |
| `orderInputChannel`     | Receives messages before filtering.                  |
| `OrderMessageFilter`    | Evaluates whether an order should be accepted.       |
| `orderOutputChannel`    | Delivers accepted messages to downstream components. |
| `FilterSupport`         | Records messages that pass through the filter.       |

This separation makes the filtering process easier to understand, test, and extend.

---

## Integration Tests

The example contains three integration tests:

| Test                                                      | Expected behavior                                    |
|-----------------------------------------------------------|------------------------------------------------------|
| `publishOrder_shouldForwardMessageWhenQuantityIsPositive` | A positive quantity passes through the filter.       |
| `publishOrder_shouldRejectMessageWhenQuantityIsZero`      | A zero quantity is rejected.                         |
| `publishMultipleOrders_shouldForwardOnlyAcceptedMessages` | Only accepted messages reach the downstream handler. |

These tests cover both filtering outcomes and the behavior of the filter when processing multiple messages.

A separate unit test for the filter method is unnecessary because the integration tests already exercise the actual filter through Spring Integration.

---

## Filters vs Transformers

Filters and transformers serve different purposes in a Spring Integration flow.

### Filters

A filter determines whether a message should continue.

```text
Incoming Message
       │
       ▼
     Filter
       │
       ├── Accepted ──► Forward Original Message
       │
       └── Rejected ──► Not Forwarded
```

The payload is not transformed.

### Transformers

A transformer converts a message payload into another representation.

```text
Incoming Message
       │
       ▼
   Transformer
       │
       ▼
 Transformed Payload
       │
       ▼
  Output Channel
```

### Key Difference

| Filter                                        | Transformer                            |
|-----------------------------------------------|----------------------------------------|
| Determines whether a message should continue. | Converts a message payload.            |
| Returns a boolean decision.                   | Returns a transformed payload.         |
| Accepts or rejects messages.                  | Produces a new message representation. |
| Does not normally change the payload.         | Changes the payload.                   |

Both are useful for controlling how messages move through an integration flow, but they serve different responsibilities.

---

## Filters vs Message Handlers

A filter and a message handler also have different responsibilities.

### Filter

A filter evaluates a condition and determines whether a message should be forwarded.

### Message Handler

A message handler consumes a message and performs an operation.

In this example, `FilterSupport` is the downstream message handler.

It only receives messages that have passed the filter.

This demonstrates how filtering can prevent unnecessary processing by downstream components.

---

## Running the Example

From the repository root:

```bash
mvn -pl 08-spring-integration/filters spring-boot:run
```

The application starts the Spring Integration context and registers the message channels and filter endpoint.

This example does not expose an HTTP endpoint or connect to an external message broker. Its message flow is demonstrated through the publisher and integration tests.

---

## Running the Tests

Run the tests from the module:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 08-spring-integration/filters -am clean verify
```

The tests verify message acceptance, rejection, and filtering of multiple messages.

---

## Key Takeaways

- Message filtering determines whether an incoming message should continue through an integration flow.
- Spring Integration provides the `@Filter` annotation for defining filter endpoints.
- A filter method returns a boolean to indicate whether a message is accepted.
- Accepted messages are forwarded to the configured output channel.
- Rejected messages are not forwarded by default.
- A rejected message does not automatically cause an exception.
- The `discardChannel` attribute can route rejected messages to another channel.
- `throwExceptionOnRejection` can be used to raise an exception when a message is rejected.
- `DirectChannel` provides synchronous message delivery.
- Filters do not normally modify the message payload.
- Integration tests can verify both accepted and rejected message behavior.

---

## Next Example

The next example will explore **Routers** in Spring Integration, including how messages can be directed to different channels based on their payload or message headers.
