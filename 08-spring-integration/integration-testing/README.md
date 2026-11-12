# Integration Testing

This example demonstrates **integration testing** in Spring Integration.

It focuses on how to test Spring Integration components individually, verify message delivery through channels, test complete integration flows, validate error propagation, and verify asynchronous message processing.

The example uses an order-processing scenario to demonstrate both unit testing and integration testing techniques.

## What This Example Demonstrates

- What integration testing means in Spring Integration.
- The difference between unit testing and integration testing.
- Testing complete Spring Integration flows.
- Testing message transformers independently.
- Testing service activators and business logic.
- Testing `DirectChannel`, `QueueChannel`, and `ExecutorChannel`.
- Testing message delivery and message consumption.
- Testing exception propagation through integration flows.
- Testing asynchronous message processing.
- Using `@SpringBootTest` to load the Spring application context.
- Using `@SpringIntegrationTest` to enable Spring Integration test support.
- Using message receive timeouts instead of arbitrary delays.

---

## What Is Integration Testing?

Integration testing verifies that multiple application components work together correctly.

In Spring Integration, this means testing how messages move between channels, how endpoints process messages, and how integration components interact within an application context.

An integration flow can contain several components:

```text
Message Source
      │
      ▼
Input Channel
      │
      ▼
Transformer
      │
      ▼
Service Activator
      │
      ▼
Output Channel
      │
      ▼
Message Consumer
```

Testing the complete flow verifies that these components work together as expected.

---

## Unit Testing vs Integration Testing

Unit testing and integration testing serve different purposes.

### Unit Testing

Unit tests verify individual components independently of the Spring application context.

For example, the `OrderTransformer` can be tested directly:

```java
OrderTransformer transformer = new OrderTransformer();
OrderSummary summary = transformer.transform(new OrderMessage("ORDER-001", 3));
```

This verifies the transformation logic without requiring a running Spring application.

Unit tests are useful for checking:

- Business logic.
- Payload transformations.
- Input validation.
- Exception handling within individual components.

### Integration Testing

Integration tests verify how Spring-managed components interact.

For example, a test can send a message through the input channel and inspect the result delivered to the output channel.

```java
boolean sent = ordersInputChannel.send(message);

assertTrue(sent);

Message<?> receivedMessage = ordersOutputChannel.receive(1_000L);
```

This verifies that the configured integration flow processes the message and delivers the expected result.

### Comparison

| Unit Testing                  | Integration Testing                        |
|-------------------------------|--------------------------------------------|
| Tests individual components   | Tests interactions between components      |
| Usually does not start Spring | Often loads the Spring application context |
| Focuses on isolated behavior  | Focuses on component collaboration         |
| Uses direct method calls      | Uses actual messaging infrastructure       |
| Generally faster              | Usually involves more application setup    |

Both approaches are important when testing Spring Integration applications.

---

## Order Processing Scenario

The example uses a simple order-processing scenario.

An incoming order contains an order identifier and quantity.

```java
public record OrderMessage(String orderId, int quantity) {
}
```

The transformer converts the incoming order into an order summary:

```java
public record OrderSummary(String orderId, int quantity, String description) {
}
```

The service activator processes the resulting summary and returns an updated result.

The processing service rejects orders with non-positive quantities.

This provides both successful and unsuccessful processing scenarios for the tests.

---

## Integration Flow Configuration

The example defines two integration flows:

- A synchronous order-processing flow.
- An asynchronous order-processing flow.

Both flows reuse the same transformer and processing service.

### Synchronous Flow

The synchronous flow uses a `DirectChannel` as its input channel.

```java
@Bean
public IntegrationFlow orderIntegrationFlow(OrderTransformer orderTransformer, OrderProcessingService orderProcessingService) {

    return IntegrationFlow.from("ordersInputChannel").transform(orderTransformer).handle(orderProcessingService, "process")
            .channel("ordersOutputChannel").get();
}
```

The flow performs the following operations:

1. Receives an order message.
2. Transforms the order into an order summary.
3. Invokes the order processing service.
4. Sends the processed summary to the output channel.

Conceptually:

```text
OrderMessage
     │
     ▼
ordersInputChannel
     │
     ▼
OrderTransformer
     │
     ▼
OrderProcessingService
     │
     ▼
ordersOutputChannel
     │
     ▼
OrderSummary
```

### Asynchronous Flow

The asynchronous flow uses an `ExecutorChannel` as its input channel.

```java
@Bean
public MessageChannel asyncOrdersInputChannel(@Qualifier("orderTaskExecutor") TaskExecutor executor) {

    return new ExecutorChannel(executor);
}
```

The flow is configured separately:

```java
@Bean
public IntegrationFlow asyncOrderIntegrationFlow(OrderTransformer orderTransformer,
        OrderProcessingService orderProcessingService) {

    return IntegrationFlow.from("asyncOrdersInputChannel").transform(orderTransformer).handle(orderProcessingService, "process")
            .channel("asyncOrdersOutputChannel").get();
}
```

An `ExecutorChannel` dispatches message handling to an executor, allowing processing to happen on a different thread from the sender.

Conceptually:

```text
Sending Thread
      │
      ▼
asyncOrdersInputChannel
      │
      ▼
ExecutorChannel
      │
      ▼
Task Executor
      │
      ▼
Integration Flow
      │
      ▼
asyncOrdersOutputChannel
```

This flow demonstrates how asynchronous messaging can be tested without relying on arbitrary thread delays.

---

## Testing Complete Integration Flows

The `IntegrationFlowTest` class verifies that an order successfully travels through the synchronous integration flow.

The test uses:

```java
@SpringBootTest
@SpringIntegrationTest
class IntegrationFlowTest {
}
```

### `@SpringBootTest`

`@SpringBootTest` loads the Spring application context.

This allows the test to use actual Spring-managed components, including:

- Integration flow beans.
- Message channels.
- Transformers.
- Business services.

### `@SpringIntegrationTest`

`@SpringIntegrationTest` enables Spring Integration's test infrastructure.

It is useful when tests need Spring Integration-specific test support.

The example includes this annotation to introduce Spring Integration's testing support alongside Spring Boot's application-context testing.

### Sending a Message

The test creates an order and wraps it in a Spring message:

```java
OrderMessage order = new OrderMessage("ORDER-001", 3);
Message<OrderMessage> message = MessageBuilder.withPayload(order).build();
```

The message is sent through the input channel:

```java
boolean sent = ordersInputChannel.send(message);

assertTrue(sent);
```

### Receiving the Result

The test retrieves the processed message:

```java
Message<?> receivedMessage = ordersOutputChannel.receive(RECEIVE_TIMEOUT);
```

It then verifies the resulting payload:

```java
OrderSummary summary = (OrderSummary) receivedMessage.getPayload();

assertEquals("ORDER-001", summary.orderId());
assertEquals(3, summary.quantity());
assertEquals("Processed: Order containing 3 items", summary.description());
```

This verifies that the message has passed through both the transformer and service activator.

---

## Testing Transformers

The `TransformerTest` class verifies the transformation logic independently of the Spring application context.

The transformer converts an `OrderMessage` into an `OrderSummary`.

```java
OrderMessage order = new OrderMessage("ORDER-001", 3);
OrderSummary summary = orderTransformer.transform(order);
```

The test verifies:

- The order identifier is preserved.
- The quantity is preserved.
- The expected description is generated.

### Testing Unsupported Payloads

The transformer also rejects unsupported payload types:

```java
IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderTransformer.transform("INVALID"));
```

This demonstrates how unit tests can verify both successful transformation and invalid input handling.

---

## Testing Service Activators

The `ServiceActivatorTest` class tests the business logic implemented by `OrderProcessingService`.

The service processes an order summary and returns an updated summary.

```java
OrderSummary order = new OrderSummary("ORDER-001", 3, "Order containing 3 items");
OrderSummary result = orderProcessingService.process(order);
```

The test verifies that the processed description is correct.

### Testing Business Validation

The service rejects orders whose quantity is zero or negative.

```java
OrderSummary order = new OrderSummary("ORDER-001", 0, "Order containing 0 items");

assertThrows(IllegalArgumentException.class,() -> orderProcessingService.process(order));
```

This demonstrates how to test the business logic behind a service activator independently of the messaging infrastructure.

---

## Testing Message Channels

Spring Integration provides different channel implementations with different messaging behavior.

The `ChannelTest` class demonstrates how to test the channels used in the example.

### DirectChannel

A `DirectChannel` delivers messages to its subscribers synchronously.

The test subscribes a message handler:

```java
DirectChannel channel = new DirectChannel();

AtomicReference<Message<?>> receivedMessage = new AtomicReference<>();

channel.subscribe(receivedMessage::set);
```

It then sends a message:

```java
boolean sent = channel.send(message);

assertTrue(sent);
assertEquals(order, receivedMessage.get().getPayload());
```

This verifies that the subscriber receives the message.

### QueueChannel

A `QueueChannel` stores messages until a consumer retrieves them.

The test sends a message:

```java
boolean sent = channel.send(message);

assertTrue(sent);
```

It then retrieves the message:

```java
Message<?> receivedMessage = channel.receive(1_000L);
```

This demonstrates the queue-based messaging behavior of `QueueChannel`.

### ExecutorChannel

An `ExecutorChannel` dispatches message handling through an executor.

The example verifies that the asynchronous input channel is configured with the expected channel implementation:

```java
assertInstanceOf(ExecutorChannel.class, asyncOrdersInputChannel);
```

The actual asynchronous message processing behavior is tested separately in `AsynchronousFlowTest`.

### Channel Comparison

| Channel           | Behavior                                        |
|-------------------|-------------------------------------------------|
| `DirectChannel`   | Synchronous message delivery                    |
| `QueueChannel`    | Stores messages until they are received         |
| `ExecutorChannel` | Dispatches message handling through an executor |

Understanding these differences is important when writing reliable messaging tests.

---

## Testing Error Propagation

The `IntegrationFlowErrorTest` class verifies what happens when an exception occurs during message processing.

The example submits an order with an invalid quantity:

```java
OrderMessage order = new OrderMessage("ORDER-001", 0);
```

The transformer successfully converts the message, but the service activator rejects the resulting order summary.

The test verifies that the exception propagates:

```java
MessageHandlingException exception = assertThrows(MessageHandlingException.class, () -> ordersInputChannel.send(message));
```

The underlying business exception can then be inspected:

```java
assertInstanceOf(IllegalArgumentException.class, exception.getCause());

assertEquals("Order quantity must be greater than zero", exception.getCause().getMessage());
```

It also verifies that no successful result is delivered to the output channel:

```java
assertNull(ordersOutputChannel.receive(0));
```

This demonstrates an important testing principle: a failed message-processing operation should not be mistaken for a successful message delivery.

---

## Testing Asynchronous Message Processing

The `AsynchronousFlowTest` class verifies that the asynchronous integration flow processes messages successfully.

Unlike the synchronous flow, an `ExecutorChannel` dispatches processing to another thread.

Therefore, the test must account for the fact that the output may not be available immediately after sending the message.

### Sending an Asynchronous Message

The test sends an order through the asynchronous input channel:

```java
boolean sent = asyncOrdersInputChannel.send(message);

assertTrue(sent);
```

### Waiting for the Result

The test waits for the processed message:

```java
Message<?> receivedMessage = asyncOrdersOutputChannel.receive(RECEIVE_TIMEOUT);
```

It then verifies the result.

### Why Use a Receive Timeout?

Asynchronous processing introduces timing differences between message submission and message consumption.

Using:

```java
asyncOrdersOutputChannel.receive(3_000L);
```

allows the test to wait for a message up to the configured timeout.

This is preferable to using:

```java
Thread.sleep(1_000);
```

A fixed sleep does not guarantee that processing has completed. It can also make tests slower than necessary.

A receive timeout provides a bounded wait tied directly to the expected messaging outcome.

---

## Testing Strategies Used in This Example

The example combines two testing approaches.

### 1. Isolated Unit Tests

Used by:

- `TransformerTest`
- `ServiceActivatorTest`

These tests instantiate the components directly and verify their behavior without starting Spring.

### 2. Spring Integration Tests

Used by:

- `IntegrationFlowTest`
- `ChannelTest`
- `IntegrationFlowErrorTest`
- `AsynchronousFlowTest`

These tests load the application context and verify the actual Spring-managed messaging infrastructure.

The distinction is important because not every component requires a complete application context to test its behavior.

---

## Test Coverage

The example contains six test classes covering the following scenarios:

| Test Class                 | What It Tests                                          |
|----------------------------|--------------------------------------------------------|
| `IntegrationFlowTest`      | Complete synchronous integration flow                  |
| `TransformerTest`          | Successful transformation and unsupported payloads     |
| `ServiceActivatorTest`     | Successful processing and invalid quantities           |
| `ChannelTest`              | Channel types, direct delivery, and queued messages    |
| `IntegrationFlowErrorTest` | Exception propagation and absence of successful output |
| `AsynchronousFlowTest`     | Asynchronous message processing and output delivery    |

Together, these tests demonstrate how to test individual components, messaging infrastructure, and complete integration flows.

---

## Running the Example

From the repository root:

```bash
mvn -pl 08-spring-integration/integration-testing spring-boot:run
```

The application starts with both synchronous and asynchronous order-processing flows configured.

---

## Running the Tests

Run the tests from the module:

```bash
mvn -pl 08-spring-integration/integration-testing test
```

Or run the module and its required Maven dependencies from the repository root:

```bash
mvn -pl 08-spring-integration/integration-testing -am clean verify
```

---

## Key Takeaways

- Integration testing verifies that application components work together correctly.
- Unit tests are useful for testing individual transformers and business services.
- `@SpringBootTest` loads the Spring application context for integration tests.
- `@SpringIntegrationTest` enables Spring Integration test support.
- `DirectChannel` delivers messages synchronously.
- `QueueChannel` stores messages until they are consumed.
- `ExecutorChannel` dispatches message handling through an executor.
- Complete integration flows can be tested by sending messages through input channels and inspecting output channels.
- Error propagation should be tested alongside successful processing.
- Asynchronous tests should wait for actual messaging outcomes rather than relying on arbitrary sleeps.
- A combination of unit tests and integration tests provides more meaningful coverage than relying on either approach alone.

---

## Next Example

This example completes the planned **Spring Integration** examples in Module 08.
