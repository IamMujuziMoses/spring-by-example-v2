# Transformers

This example demonstrates **message transformation** in Spring Integration.

It focuses on how Spring Integration transforms an incoming message payload into a different representation, how `@Transformer` defines a transformation endpoint, and how transformed messages are forwarded to an output channel.

The example receives an `OrderMessage`, transforms it into an `OrderSummary`, and sends the resulting message to an output channel for further processing.

## What This Example Demonstrates

- What message transformation means in Spring Integration.
- The role of transformers in a message-driven application.
- Spring Integration's `@Transformer` annotation.
- Configuring input and output message channels.
- Transforming message payloads from one type to another.
- Preserving message headers during transformation.
- Connecting a transformer to a downstream message handler.
- Testing message transformation with Spring Boot test support.
- Validating the payload received by a downstream message handler.

---

## What Is Message Transformation?

Message transformation is the process of converting a message payload from one representation into another.

In an integration application, different components may require different data representations.

For example, an application may receive an order containing individual product details but need a simplified summary for another component.

A transformer performs this conversion without requiring the sending component to know how the receiving component expects the data to be structured.

A simplified flow looks like:

```text
Incoming Message
       │
       ▼
OrderMessage
       │
       ▼
Transformer
       │
       ▼
OrderSummary
       │
       ▼
Output Channel
       │
       ▼
Downstream Handler
```

The transformer receives an incoming message, converts its payload, and produces a new message containing the transformed payload.

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

For example, the `source` header in this example identifies where an order originated.

The transformer changes the payload while allowing relevant message metadata to continue through the integration flow.

---

## OrderMessage

The example uses an `OrderMessage` record to represent an incoming order.

```java
public record OrderMessage(long orderId, String product, int quantity, BigDecimal unitPrice) {
}
```

It contains:

- `orderId`: The order identifier.
- `product`: The product name.
- `quantity`: The number of items ordered.
- `unitPrice`: The price of one item.

For example:

```java
new OrderMessage(1L, "Laptop", 2, new BigDecimal("1200.00"));
```

This represents an order for two laptops, each costing 1200.00.

---

## OrderSummary

The transformer converts the incoming `OrderMessage` into an `OrderSummary`.

```java
public record OrderSummary(long orderId, String description, BigDecimal totalPrice) {
}
```

The summary contains:

- `orderId`: The original order identifier.
- `description`: A simplified description of the order.
- `totalPrice`: The total cost of the order.

For the example order:

```text
OrderMessage
  orderId: 1
  product: Laptop
  quantity: 2
  unitPrice: 1200.00
```

The resulting summary is:

```text
OrderSummary
  orderId: 1
  description: 2 x Laptop
  totalPrice: 2400.00
```

The transformer produces a new representation that can be consumed by downstream components without requiring them to understand the original order structure.

---

## The `@Transformer` Annotation

Spring Integration provides the `@Transformer` annotation to identify a method that transforms messages.

The example uses:

```java
@MessageEndpoint
public class OrderMessageTransformer {

    @Transformer(inputChannel = "orderInputChannel", outputChannel = "orderOutputChannel")
    public OrderSummary transform(OrderMessage order) {
        String description = order.quantity() + " x " + order.product();

        BigDecimal totalPrice = order.unitPrice().multiply(BigDecimal.valueOf(order.quantity()));

        return new OrderSummary(order.orderId(), description, totalPrice);
    }

}
```

The `@MessageEndpoint` annotation identifies the class as a Spring Integration message endpoint.

The `@Transformer` annotation identifies the transformation method and connects it to the integration flow.

It defines two channels:

- `inputChannel`: The channel from which incoming messages are received.
- `outputChannel`: The channel to which transformed messages are sent.

The method receives the incoming `OrderMessage` payload and returns an `OrderSummary`.

Spring Integration uses the returned object as the payload of the output message.

The transformer does not need to manually create or send the output message.

---

## Input and Output Channels

The example uses two `DirectChannel` instances:

- `orderInputChannel`
- `orderOutputChannel`

They represent the entry and exit points of the transformation.

The channels are configured in `TransformerConfiguration`.

```java
@Configuration
@EnableIntegration
public class TransformerConfiguration {

    @Bean
    public MessageChannel orderInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel orderOutputChannel(TransformerSupport support) {
        DirectChannel channel = new DirectChannel();
        channel.subscribe(support);

        return channel;
    }

}
```

### Input Channel

The `orderInputChannel` receives incoming order messages.

The transformer subscribes to this channel through its `@Transformer` configuration.

```text
Publisher
    │
    ▼
orderInputChannel
    │
    ▼
OrderMessageTransformer
```

### Output Channel

The `orderOutputChannel` receives messages produced by the transformer.

The example explicitly subscribes `TransformerSupport` to this channel.

```text
OrderMessageTransformer
          │
          ▼
   orderOutputChannel
          │
          ▼
    TransformerSupport
```

This demonstrates how a transformer can be connected to another message-handling component through a message channel.

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
Transformer
       │
       ▼
Output DirectChannel
       │
       ▼
Downstream Handler
```

The processing flow is synchronous.

This makes `DirectChannel` suitable for a small example where the goal is to demonstrate message transformation without introducing asynchronous processing or external messaging infrastructure.

---

## The Transformation Process

The transformation method performs two operations.

### 1. Create the Order Description

```java
String description = order.quantity() + " x " + order.product();
```

This combines the order quantity and product name into a readable description.

For example:

```text
2 x Laptop
```

### 2. Calculate the Total Price

```java
BigDecimal totalPrice = order.unitPrice().multiply(BigDecimal.valueOf(order.quantity()));
```

The calculation multiplies the unit price by the quantity.

For example:

```text
1200.00 × 2 = 2400.00
```

`BigDecimal` is used for monetary calculations rather than `double` or `float`, avoiding the typical binary floating-point precision problems associated with those types.

### 3. Return the Transformed Payload

```java
return new OrderSummary(order.orderId(), description, totalPrice);
```

The transformer returns the new `OrderSummary`.

Spring Integration forwards the returned object as the payload of a message to the configured output channel.

---

## Publishing Messages

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

The publisher adds a `source` header to identify the origin of the order.

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

The publisher does not perform the transformation itself.

Its responsibility is to construct and send the message to the input channel.

This keeps message publishing separate from transformation logic.

---

## Message Headers During Transformation

A message contains both a payload and headers.

In this example, the publisher adds a `source` header:

```java
.setHeader("source", source)
```

The transformer changes the payload from `OrderMessage` to `OrderSummary`.

Spring Integration's transformer infrastructure normally carries message headers forward when creating the transformed message, subject to header propagation rules and any explicit header changes.

This allows downstream components to access metadata associated with the original message.

For example:

```text
Incoming Message
  │
  ├── Payload: OrderMessage
  │
  └── Header: source = "web"
          │
          ▼
      Transformer
          │
          ▼
Outgoing Message
  │
  ├── Payload: OrderSummary
  │
  └── Header: source = "web"
```

The integration tests verify that the `source` header is available to the downstream handler after transformation.

---

## Downstream Message Handling

After transformation, the resulting message is sent to `orderOutputChannel`.

The example uses `TransformerSupport` as the downstream message handler.

It implements Spring Messaging's `MessageHandler` interface:

```java
public class TransformerSupport implements MessageHandler {

    @Override
    public void handleMessage(Message<?> message) {
        if (!(message.getPayload() instanceof OrderSummary summary)) {
            throw new IllegalArgumentException("Unsupported message payload");
        }

        // Process the transformed order summary.
    }

}
```

The handler validates that the received payload is an `OrderSummary`.

This is important because the downstream component expects the transformed representation rather than the original `OrderMessage`.

If an unsupported payload is received, the handler throws an `IllegalArgumentException`.

The example keeps this validation separate from the transformation logic.

The transformer is responsible for converting the message, while the downstream handler is responsible for consuming the resulting message.

---

## Transformation Flow

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
OrderMessageTransformer
        │
        │ Transform payload
        │ OrderMessage → OrderSummary
        ▼
  orderOutputChannel
        │
        ▼
  TransformerSupport
        │
        │ Validate OrderSummary
        │ Read source header
        ▼
 Record Processed Order
```

The responsibilities are separated:

| Component                 | Responsibility                                          |
|---------------------------|---------------------------------------------------------|
| `OrderMessagePublisher`   | Creates and publishes incoming order messages.          |
| `orderInputChannel`       | Receives messages before transformation.                |
| `OrderMessageTransformer` | Converts `OrderMessage` into `OrderSummary`.            |
| `orderOutputChannel`      | Delivers transformed messages to downstream components. |
| `TransformerSupport`      | Validates and records transformed messages.             |

This separation makes the transformation flow easier to understand, test, and extend.

---

## Testing Message Transformation

The example uses Spring Boot's test support and JUnit to verify the integration flow.

The tests use the actual publisher, channels, transformer, and downstream handler.

### Transforming a Message

The first test verifies that an incoming order is transformed into the expected summary.

For example:

```java
OrderMessage order = new OrderMessage(1L, "Laptop", 2, new BigDecimal("1200.00"));

publisher.publish(order, "web");
```

The expected result is:

```text
OrderSummary
  orderId: 1
  description: 2 x Laptop
  totalPrice: 2400.00
```

The test verifies that the transformed summary is received by the downstream handler.

### Transforming Multiple Messages

The second test publishes multiple orders.

It verifies that:

- Each message is transformed independently.
- Each order produces the correct summary.
- The resulting summaries are processed in the expected order.

This demonstrates that the transformer can process multiple messages through the same integration flow.

### Preserving Message Headers

The third test verifies that the `source` header survives the transformation.

The publisher adds the header:

```java
publisher.publish(order, "mobile");
```

The downstream handler receives the transformed message and reads the header.

The test verifies that the original source information remains available after transformation.

### Rejecting Unsupported Payloads

The unit test for `TransformerSupport` verifies its payload validation.

It sends a message containing a `String` instead of an `OrderSummary`:

```java
Message<String> message = MessageBuilder.withPayload("Unsupported payload").build();
```

It then verifies that the handler rejects the message:

```java
assertThatThrownBy(() -> support.handleMessage(message)).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unsupported message payload");
```

This test focuses on the downstream handler's validation logic rather than the entire Spring Integration flow.

---

## Unit Testing vs Integration Testing

The example uses both unit and integration testing.

### Integration Tests

The integration tests verify the complete message transformation flow.

They cover:

- Message publishing.
- Transformation from `OrderMessage` to `OrderSummary`.
- Processing multiple messages.
- Message header propagation.

### Unit Test

The unit test verifies the unsupported-payload validation in `TransformerSupport`.

It directly invokes the handler without starting a Spring application context.

This keeps the test focused on the validation behavior.

---

## Transformers vs Message Handlers

Transformers and message handlers serve different purposes in a Spring Integration flow.

### Message Handler

A message handler consumes a message and performs an operation.

```text
Message
   │
   ▼
Message Handler
   │
   ▼
Process Message
```

A handler does not necessarily produce another message.

### Transformer

A transformer converts a message payload into another representation.

```text
Incoming Message
      │
      ▼
  Transformer
      │
      ▼
 Transformed Message
```

The transformed message can then be sent to another channel for further processing.

### Key Difference

| Message Handler                                        | Transformer                                        |
|--------------------------------------------------------|----------------------------------------------------|
| Consumes messages.                                     | Converts message payloads.                         |
| Usually performs an operation or delegates processing. | Produces a transformed payload.                    |
| Does not necessarily produce an output message.        | Sends the transformed result to an output channel. |
| Commonly implemented using `MessageHandler`.           | Commonly configured using `@Transformer`.          |

A transformer can be considered a specialized message-processing endpoint whose primary responsibility is conversion.

---

## Why Separate Transformation from Business Processing?

The example keeps transformation logic separate from downstream message handling.

The transformer only converts the incoming order into a summary.

The downstream handler receives and processes the resulting summary.

This separation provides several benefits:

- Each component has a focused responsibility.
- Transformation logic can be tested independently.
- Downstream components do not need to understand the original message representation.
- Different message formats can be converted into a common representation.
- The integration flow can be extended without putting all processing logic into one component.

For example, another component could consume `OrderSummary` and publish it to a reporting system without needing to know how the original order was received.

---

## Running the Example

From the repository root:

```bash
mvn -pl 08-spring-integration/transformers spring-boot:run
```

The application starts the Spring Integration context and registers the message channels and transformer endpoint.

This example does not expose an HTTP endpoint or connect to an external message broker. Its message flow is demonstrated through the publisher and integration tests.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 08-spring-integration/transformers -am clean verify
```

The tests verify the transformation flow and the downstream handler's payload validation.

---

## Key Takeaways

- Message transformation converts a message payload from one representation into another.
- Spring Integration provides the `@Transformer` annotation for defining transformation endpoints.
- A transformer can receive messages from an input channel and send transformed messages to an output channel.
- The transformer method's return value becomes the output message payload.
- Message headers carry metadata alongside the payload.
- Spring Integration normally propagates headers through transformer endpoints, subject to header propagation rules.
- `DirectChannel` provides synchronous message delivery.
- Transformation logic should remain separate from downstream business processing.
- A downstream handler can validate the type of payload it receives.
- Unit tests and integration tests can verify different responsibilities within a message transformation flow.

---

## Next Example

The next example will explore **Message Filters** in Spring Integration, including how messages can be accepted or rejected based on conditions before continuing through an integration flow.
