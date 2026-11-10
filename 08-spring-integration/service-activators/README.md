# Service Activators

This example demonstrates **service activators** in Spring Integration.

It focuses on how Spring Integration invokes application services in response to incoming messages, how service activators process message payloads, and how their return values can be published to an output channel.

The example uses a synchronous message flow with `DirectChannel` to demonstrate request processing and response handling without requiring an external message broker.

## What This Example Demonstrates

- What service activators are in Spring Integration.
- The role of `@ServiceActivator`.
- The relationship between message channels and service activators.
- Receiving messages through an input channel.
- Delegating message processing to an application service.
- Returning processing results through an output channel.
- Request-response messaging.
- Preserving message headers during message processing.
- Testing service activator message flows.
- Validating message payloads in message handlers.

---

## What Is a Service Activator?

A service activator is a Spring Integration endpoint that connects a message channel to a method that performs application-specific processing.

It receives messages from an input channel, invokes a method, and can optionally publish the method's return value to an output channel.

Service activators are useful when integrating messaging infrastructure with ordinary application services.

For example, an application might receive an order message, calculate its total price, and publish the resulting order summary.

A simplified flow looks like:

```text
Order Publisher
      │
      ▼
Input Channel
      │
      ▼
Service Activator
      │
      ▼
Application Service
      │
      ▼
Processing Result
      │
      ▼
Output Channel
      │
      ▼
Result Handler
```

The service activator provides the connection between the messaging infrastructure and the application's business logic.

---

## The `@ServiceActivator` Annotation

Spring Integration provides `@ServiceActivator` to mark a method as a service-activating message endpoint.

Consider the following implementation:

```java
@MessageEndpoint
public class OrderMessageServiceActivator {

    private final OrderMessageService orderMessageService;

    public OrderMessageServiceActivator(OrderMessageService orderMessageService) {
        this.orderMessageService = orderMessageService;
    }

    @ServiceActivator(inputChannel = "orderInputChannel", outputChannel = "orderOutputChannel")
    public OrderProcessingResult process(OrderMessage order) {
        return orderMessageService.process(order);
    }

}
```

The annotation identifies the method as a message-handling endpoint.

### `inputChannel`

```java
inputChannel = "orderInputChannel"
```

Specifies the channel from which the service activator receives messages.

In this example, the publisher sends order messages to `orderInputChannel`.

### `outputChannel`

```java
outputChannel = "orderOutputChannel"
```

Specifies where the service activator publishes the result returned by the method.

The method returns an `OrderProcessingResult`, which Spring Integration uses as the payload of the reply message.

### `@MessageEndpoint`

```java
@MessageEndpoint
```

Identifies the class as a Spring Integration message endpoint.

It allows Spring Integration to discover and configure the annotated messaging methods through component scanning.

---

## The Message Model

The example uses an `OrderMessage` record to represent an incoming order.

```java
public record OrderMessage(long orderId, String product, int quantity, BigDecimal unitPrice) {
}
```

An order contains:

- `orderId`: Identifies the order.
- `product`: Identifies the ordered product.
- `quantity`: Specifies the number of items.
- `unitPrice`: Specifies the price of one item.

The service activator processes this message and produces an `OrderProcessingResult`.

```java
public record OrderProcessingResult(long orderId, String product, int quantity, BigDecimal totalPrice) {
}
```

The result contains the original order information and the calculated total price.

Separating the incoming message from the processing result makes the request-response flow explicit.

---

## Delegating Processing to an Application Service

The service activator does not implement the business calculation itself.

Instead, it delegates processing to `OrderMessageService`.

```java
@Service
public class OrderMessageService {

    public OrderProcessingResult process(OrderMessage order) {
        BigDecimal totalPrice = order.unitPrice().multiply(BigDecimal.valueOf(order.quantity()));

        return new OrderProcessingResult(order.orderId(), order.product(), order.quantity(), totalPrice);
    }
}
```

The service calculates the total price by multiplying the unit price by the quantity.

For example:

```text
Order ID:       101
Product:        Laptop
Quantity:       2
Unit Price:     1200.00
                │
                ▼
Total Price:    2400.00
```

This separation is intentional.

The service activator handles the messaging integration, while the application service handles the business operation.

This keeps the business logic independent of Spring Integration APIs.

---

## Configuring Message Channels

The example uses two `DirectChannel` instances.

```java
@Configuration
@EnableIntegration
public class ServiceActivatorConfiguration {

    @Bean
    public MessageChannel orderInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel orderOutputChannel(OrderProcessingSupport support) {
        DirectChannel channel = new DirectChannel();
        channel.subscribe(support);
        return channel;
    }

    @Bean
    public OrderProcessingSupport orderProcessingSupport() {
        return new OrderProcessingSupport();
    }

}
```

### `@EnableIntegration`

Enables Spring Integration infrastructure and annotation-based configuration.

### `orderInputChannel`

```java
@Bean
public MessageChannel orderInputChannel() {
    return new DirectChannel();
}
```

This is the input channel through which order messages enter the integration flow.

The service activator subscribes to this channel through its `inputChannel` attribute.

### `orderOutputChannel`

```java
@Bean
public MessageChannel orderOutputChannel(OrderProcessingSupport support) {
    DirectChannel channel = new DirectChannel();
    channel.subscribe(support);
    return channel;
}
```

This is the output channel through which processing results are published.

The example subscribes `OrderProcessingSupport` to the channel so that it can receive and record the results.

### Why `DirectChannel`?

`DirectChannel` provides point-to-point message delivery.

It invokes its subscriber synchronously in the sender's thread.

For this example, that means the message passes through the service activator and reaches the output handler as part of the same synchronous processing flow.

No external message broker or asynchronous infrastructure is required.

---

## Publishing Messages

The `OrderMessagePublisher` sends messages to the input channel.

```java
public class OrderMessagePublisher {

    private final MessageChannel orderInputChannel;

    public OrderMessagePublisher(@Qualifier("orderInputChannel") MessageChannel orderInputChannel) {
        this.orderInputChannel = orderInputChannel;
    }

    public void publish(OrderMessage order, String source) {
        Message<OrderMessage> message = MessageBuilder.withPayload(order).setHeader("source", source).build();

        orderInputChannel.send(message);
    }

}
```

The publisher uses `MessageBuilder` to construct a Spring message.

The message contains:

- The `OrderMessage` as its payload.
- A `source` header identifying where the message originated.

For example:

```java
publisher.publish(new OrderMessage(101L, "Laptop", 2, new BigDecimal("1200.00")), "web");
```

The publisher does not invoke the application service directly.

It sends the message to the input channel, allowing Spring Integration to route it to the service activator.

---

## Understanding Message Payloads and Headers

A Spring message consists of a payload and headers.

```text
Message
  │
  ├── Payload
  │     └── OrderMessage
  │
  └── Headers
        └── source = "web"
```

The payload contains the actual business data.

Headers contain additional metadata about the message.

In this example:

- The payload contains the order details.
- The `source` header identifies the message origin.

The service activator receives the `OrderMessage` payload directly because its method parameter is compatible with the payload type.

Spring Integration also manages message headers when producing the reply. The example verifies that the `source` header remains available to the downstream result handler.

---

## Processing Results

The service activator returns an `OrderProcessingResult`.

```java
@ServiceActivator(inputChannel = "orderInputChannel", outputChannel = "orderOutputChannel")
public OrderProcessingResult process(OrderMessage order) {
    return orderMessageService.process(order);
}
```

Because an output channel is configured, Spring Integration sends the returned result to `orderOutputChannel`.

The result becomes the payload of the reply message.

Conceptually:

```text
Incoming Message
      │
      ▼
OrderMessage
      │
      ▼
Service Activator
      │
      ▼
OrderMessageService
      │
      ▼
OrderProcessingResult
      │
      ▼
Output Channel
      │
      ▼
OrderProcessingSupport
```

This is an example of request-response messaging.

The incoming message represents the request, while the returned result represents the response.

---

## Handling Processing Results

The example uses `OrderProcessingSupport` as a message handler subscribed to the output channel.

It implements Spring's `MessageHandler` interface.

```java
public class OrderProcessingSupport implements MessageHandler {

    private final List<ProcessedOrder> processedOrders = new CopyOnWriteArrayList<>();

    @Override
    public void handleMessage(Message<?> message) {
        if (!(message.getPayload() instanceof OrderProcessingResult result)) {
            throw new IllegalArgumentException("Unsupported message payload");
        }

        String source = (String) message.getHeaders().get("source");

        processedOrders.add(new ProcessedOrder(result, source));
    }

    public List<ProcessedOrder> getProcessedOrders() {
        return List.copyOf(processedOrders);
    }

    public void clear() {
        processedOrders.clear();
    }

}
```

The handler performs three operations:

1. Validates that the message payload is an `OrderProcessingResult`.
2. Extracts the `source` message header.
3. Records the processing result and its source.

The `CopyOnWriteArrayList` provides thread-safe storage for the example's recorded results.

The getter returns an immutable snapshot using `List.copyOf()`.

The `clear()` method allows the recorded results to be reset between tests.

### Payload Validation

The handler uses Java pattern matching:

```java
if (!(message.getPayload() instanceof OrderProcessingResult result)) {
    throw new IllegalArgumentException("Unsupported message payload");
}
```

This ensures that only messages containing an `OrderProcessingResult` are accepted.

If a message contains an unsupported payload, the handler throws an `IllegalArgumentException` instead of attempting to process an unexpected object.

---

## The Complete Message Flow

The complete example can be represented as:

```text
OrderMessagePublisher
        │
        │ OrderMessage
        │ source = "web"
        ▼
orderInputChannel
        │
        ▼
OrderMessageServiceActivator
        │
        ▼
OrderMessageService
        │
        │ Calculates total price
        ▼
OrderProcessingResult
        │
        ▼
orderOutputChannel
        │
        ▼
OrderProcessingSupport
        │
        ▼
Recorded Processing Result
```

For example, publishing an order with a quantity of two and a unit price of `1200.00` produces an `OrderProcessingResult` with a total price of `2400.00`.

---

## Service Activators vs Message Handlers

Both service activators and message handlers process messages, but they serve different roles.

### Service Activators

A service activator connects a message channel to an application method.

It is commonly used to invoke existing application services and optionally publish their return values.

```java
@ServiceActivator(inputChannel = "orderInputChannel")
public OrderProcessingResult process(OrderMessage order) {
    return orderMessageService.process(order);
}
```

### Message Handlers

A message handler implements the `MessageHandler` interface and receives complete Spring messages.

```java
@Override
public void handleMessage(Message<?> message) {
    // Process the message.
}
```

The handler can inspect both the payload and message headers.

In this example, `OrderProcessingSupport` implements `MessageHandler` to receive and record the results published by the service activator.

The distinction is that the service activator provides an annotation-based endpoint for invoking application logic, while the message handler provides a lower-level message-processing contract.

---

## Service Activators and Request-Response Messaging

A service activator does not have to return a value.

It can be used for one-way message processing when no response is required.

However, this example explicitly configures an output channel and returns an `OrderProcessingResult`.

That makes it suitable for demonstrating request-response messaging:

```text
Request
   │
   ▼
Service Activator
   │
   ▼
Application Service
   │
   ▼
Response
```

The output channel provides the destination for the response.

This is different from a request-reply gateway, which provides a messaging abstraction for application code that needs to send a request and receive a reply.

---

## Running the Example

From the repository root:

```bash
mvn -pl 08-spring-integration/service-activators spring-boot:run
```

The application starts with the service activator and its message channels configured.

The example does not expose an HTTP endpoint or require an external message broker. Its message flow is demonstrated through the publisher and Spring Integration components.

---

## Running the Tests

Run the tests from the module:

```bash
mvn -pl 08-spring-integration/service-activators test
```

Or run the module together with its required Maven projects from the repository root:

```bash
mvn -pl 08-spring-integration/service-activators -am clean verify
```

---

## Key Takeaways

- A service activator connects a message channel to an application method.
- `@ServiceActivator` identifies a method that processes messages.
- `inputChannel` specifies where the service activator receives messages.
- `outputChannel` specifies where a returned result is published.
- Spring Integration can invoke ordinary application services from message endpoints.
- Separating message handling from business logic keeps application services independent of messaging infrastructure.
- A Spring message contains both a payload and headers.
- Service activators can support request-response messaging when they return a result and have an output channel configured.
- `DirectChannel` provides synchronous, point-to-point message delivery.
- `MessageHandler` provides a lower-level contract for handling complete Spring messages.
- Unit tests can verify business logic independently of the integration flow.
- Integration tests verify that messages move through the configured channels and endpoints as expected.

---

## Next Example

The next example will explore **Gateways** in Spring Integration, demonstrating how application code can interact with messaging flows through a gateway abstraction and receive replies without directly working with message channels.
