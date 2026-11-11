# Gateways

This example demonstrates **messaging gateways** in Spring Integration.

It focuses on how Spring Integration gateways provide an application-friendly interface to messaging infrastructure, how Java interface methods are mapped to messages, and how request-response communication is handled through gateway proxies.

The example uses `@MessagingGateway` to expose a Java interface that allows application code to submit orders and receive processing results without directly interacting with message channels.

## What This Example Demonstrates

- What messaging gateways are in Spring Integration.
- The role of `@MessagingGateway`.
- How Spring Integration creates gateway proxies.
- The relationship between gateways and message channels.
- Mapping method arguments to message payloads.
- Mapping method parameters to message headers using `@Header`.
- Sending requests through a gateway.
- Receiving replies from service activators.
- The role of reply channels in request-response messaging.
- Configuring gateway interface discovery with `@IntegrationComponentScan`.
- Testing gateway-based messaging flows.

---

## What Is a Messaging Gateway?

A messaging gateway provides an abstraction between application code and messaging infrastructure.

Instead of requiring application code to interact directly with message channels, a gateway exposes an interface with methods that represent messaging operations.

Spring Integration creates a proxy implementation of the interface and handles the underlying messaging operations.

For example, an application can submit an order through a gateway:

```java
OrderProcessingResult result = orderProcessingGateway.process(order, "web");
```

The application does not need to construct a Spring message, select a message channel, or manually receive the response.

The gateway handles these operations.

A simplified flow looks like:

```text
Application Code
       │
       ▼
Messaging Gateway
       │
       ▼
Input Channel
       │
       ▼
Message Endpoint
       │
       ▼
Application Service
       │
       ▼
Processing Result
       │
       ▼
Reply Channel
       │
       ▼
Messaging Gateway
       │
       ▼
Application Code
```

This allows application code to interact with messaging infrastructure through ordinary Java method calls.

---

## The `@MessagingGateway` Annotation

Spring Integration provides `@MessagingGateway` to identify an interface as a messaging gateway.

Consider the following implementation:

```java
@MessagingGateway(defaultRequestChannel = "orderInputChannel")
public interface OrderProcessingGateway {

    OrderProcessingResult process(OrderMessage order, @Header("source") String source);
}
```

The annotation instructs Spring Integration to create a proxy implementation of the interface.

The generated proxy handles the conversion of method invocations into messages and the delivery of those messages to the configured channels.

### `defaultRequestChannel`

```java
@MessagingGateway(defaultRequestChannel = "orderInputChannel")
```

Specifies the default channel to which gateway requests are sent.

In this example, every invocation of `process()` sends its request to `orderInputChannel`.

The gateway therefore does not need to explicitly reference a `MessageChannel` in application code.

### Gateway Method

```java
OrderProcessingResult process(OrderMessage order, @Header("source") String source);
```

The method defines the gateway's application-facing contract.

It accepts an `OrderMessage` and a source identifier, then returns an `OrderProcessingResult`.

The method signature describes the request and expected response without exposing the underlying messaging implementation.

---

## Gateway Method Arguments and Message Mapping

Spring Integration maps gateway method arguments to message components.

In this example:

```java
OrderProcessingResult process(OrderMessage order, @Header("source") String source);
```

The `OrderMessage` becomes the message payload.

The `source` argument becomes a message header.

Conceptually, the generated message looks like:

```text
Message
  │
  ├── Payload
  │     └── OrderMessage
  │
  └── Headers
        └── source = "web"
```

This mapping allows application code to provide both business data and message metadata through a single method invocation.

---

## Understanding `@Header`

Spring Integration provides `@Header` to map a method parameter to a message header.

```java
@Header("source") String source
```

This tells Spring Integration to place the value of `source` in the message headers rather than including it in the payload.

For example:

```java
orderProcessingGateway.process(order, "web");
```

Produces a message conceptually equivalent to:

```java
Message<OrderMessage> message = MessageBuilder.withPayload(order).setHeader("source", "web").build();
```

The gateway handles this mapping automatically.

The service activator can then access the header using:

```java
@Header("source") String source
```

This demonstrates how message metadata can be passed through a gateway without manually constructing a message.

---

## Gateway Configuration

The example uses the following configuration:

```java
@Configuration
@EnableIntegration
@IntegrationComponentScan(basePackageClasses = OrderProcessingGateway.class)
public class GatewayConfiguration {

}
```

### `@EnableIntegration`

```java
@EnableIntegration
```

Enables Spring Integration infrastructure and annotation-based configuration.

It provides the infrastructure required to configure and execute integration components.

### `@IntegrationComponentScan`

```java
@IntegrationComponentScan(basePackageClasses = OrderProcessingGateway.class)
```

Discovers interfaces annotated with `@MessagingGateway` and registers their gateway proxies as Spring beans.

This is important because gateway interfaces do not have concrete implementations in the application.

Spring Integration generates the implementation dynamically.

The `basePackageClasses` attribute identifies the package containing `OrderProcessingGateway`.

Using a class reference avoids hardcoding the package name as a string.

### Why Is `@IntegrationComponentScan` Necessary?

Spring Boot's `@SpringBootApplication` performs ordinary Spring component scanning.

However, Spring Integration gateway interfaces require gateway-specific discovery.

`@IntegrationComponentScan` handles this responsibility.

Without it, the gateway interface might not be registered as a Spring bean, resulting in an error when attempting to inject it.

For example:

```java
@Autowired
private OrderProcessingGateway orderProcessingGateway;
```

Spring must have a generated gateway proxy available to satisfy this dependency.

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

The gateway sends this record as the message payload.

The service processes the order and produces an `OrderProcessingResult`.

```java
public record OrderProcessingResult(long orderId, String product, int quantity, BigDecimal totalPrice, String source) {
}
```

The result contains the original order information, the calculated total price, and the source of the order message.

Separating the incoming message from the processing result makes the request-response flow explicit.

---

## Processing Messages with a Service Activator

The gateway sends requests to `orderInputChannel`.

The example uses a service activator to receive those requests and invoke the application service.

```java
@MessageEndpoint
public class OrderMessageServiceActivator {

    private final OrderMessageService orderMessageService;

    public OrderMessageServiceActivator(OrderMessageService orderMessageService) {
        this.orderMessageService = orderMessageService;
    }

    @ServiceActivator(inputChannel = "orderInputChannel")
    public OrderProcessingResult process(OrderMessage order, @Header("source") String source) {
        return orderMessageService.process(order, source);
    }

}
```

### `@MessageEndpoint`

Identifies the class as a Spring Integration message endpoint.

It allows Spring Integration to discover and configure the annotated messaging method.

### `@ServiceActivator`

```java
@ServiceActivator(inputChannel = "orderInputChannel")
```

Connects the annotated method to the specified input channel.

When the gateway sends a message to `orderInputChannel`, the service activator receives it and invokes the `process()` method.

### Processing the Message

```java
public OrderProcessingResult process(OrderMessage order, @Header("source") String source) {
    return orderMessageService.process(order, source);
}
```

The method receives the message payload and the `source` header.

It delegates the actual processing to `OrderMessageService` and returns the resulting `OrderProcessingResult`.

Notice that the service activator does not explicitly declare an `outputChannel`.

This is intentional because the gateway uses request-response messaging.

Spring Integration provides a reply channel for the gateway request. The service activator's return value is sent to that reply channel, allowing the gateway to receive the result.

---

## Application Service

The example delegates business processing to `OrderMessageService`.

```java
@Service
public class OrderMessageService {

    public OrderProcessingResult process(OrderMessage order, String source) {
        BigDecimal totalPrice = order.unitPrice().multiply(BigDecimal.valueOf(order.quantity()));

        return new OrderProcessingResult(order.orderId(), order.product(), order.quantity(), totalPrice, source);
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
Source:         web
```

The service returns an `OrderProcessingResult` containing the calculated total.

Separating business logic from messaging infrastructure keeps the application service independent of Spring Integration APIs.

---

## Understanding Request-Response Messaging

The gateway example demonstrates request-response messaging.

The application invokes a gateway method and expects a result.

```java
OrderProcessingResult result = orderProcessingGateway.process(order, "web");
```

The gateway sends the request through the configured input channel and waits for the corresponding reply.

Conceptually:

```text
Application
    │
    │ process(order, "web")
    ▼
Gateway
    │
    │ Request Message
    ▼
orderInputChannel
    │
    ▼
Service Activator
    │
    ▼
OrderMessageService
    │
    │ OrderProcessingResult
    ▼
Reply Channel
    │
    ▼
Gateway
    │
    │ Returns result
    ▼
Application
```

The application receives the result as the return value of the gateway method.

---

## Understanding Reply Channels

A reply channel provides the destination for a response message.

When a gateway sends a request that expects a reply, Spring Integration can establish a temporary reply channel and include it in the request message headers.

The service activator returns an object:

```java
return orderMessageService.process(order, source);
```

Spring Integration converts that return value into a reply message and sends it to the available reply channel.

The gateway receives the reply and returns its payload to the calling application.

Conceptually:

```text
Gateway Request
      │
      ▼
Request Message
      │
      ├── Payload: OrderMessage
      │
      └── Headers:
             replyChannel
             source
      │
      ▼
Service Activator
      │
      ▼
Processing Result
      │
      ▼
Reply Channel
      │
      ▼
Gateway Response
```

This example relies on Spring Integration's gateway-managed reply handling rather than defining a separate output channel.

This is one of the important differences between the Gateway example and the previous Service Activators example.

---

## Gateway vs Direct Message Publishing

A gateway and direct message publishing provide different levels of abstraction.

### Direct Message Publishing

In the previous examples, application code interacts directly with a message channel.

For example:

```java
Message<OrderMessage> message = MessageBuilder.withPayload(order).setHeader("source", "web").build();

orderInputChannel.send(message);
```

Application code is responsible for constructing the message and sending it to the channel.

### Messaging Gateway

With a gateway, application code invokes a method:

```java
OrderProcessingResult result = orderProcessingGateway.process(order, "web");
```

Spring Integration handles message construction, channel interaction, and reply handling.

| Direct Message Publishing                                        | Messaging Gateway                                            |
|------------------------------------------------------------------|--------------------------------------------------------------|
| Uses `MessageChannel` directly.                                  | Uses a Java interface.                                       |
| Application constructs messages.                                 | Spring Integration maps method arguments to messages.        |
| Application sends messages to channels.                          | Gateway sends messages to configured channels.               |
| Reply handling must be managed through messaging infrastructure. | Gateway provides a method return value for the reply.        |
| Messaging infrastructure is visible to application code.         | Messaging infrastructure is abstracted behind the interface. |

Gateways are useful when application code should interact with messaging infrastructure through a simpler application-facing API.

---

## Gateway Message Flow

A simplified view of the complete example is:

```text
Application Code
       │
       ▼
OrderProcessingGateway
       │
       │ Maps method arguments
       ▼
Request Message
       │
       ├── Payload: OrderMessage
       │
       └── Header: source
       │
       ▼
orderInputChannel
       │
       ▼
OrderMessageServiceActivator
       │
       ▼
OrderMessageService
       │
       ▼
OrderProcessingResult
       │
       ▼
Reply Channel
       │
       ▼
OrderProcessingGateway
       │
       ▼
Application Receives Result
```

---

## Running the Example

From the repository root:

```bash
mvn -pl 08-spring-integration/gateways spring-boot:run
```

The application starts with the gateway proxy and message processing infrastructure configured.

The example does not expose an HTTP endpoint or require an external message broker. Its messaging flow is demonstrated through the gateway interface and Spring Integration components.

---

## Running the Tests

Run the tests from the module:

```bash
mvn -pl 08-spring-integration/gateways test
```

Or run the module together with its required Maven projects from the repository root:

```bash
mvn -pl 08-spring-integration/gateways -am clean verify
```

---

## Key Takeaways

- A messaging gateway provides an application-facing abstraction over messaging infrastructure.
- `@MessagingGateway` identifies an interface as a Spring Integration gateway.
- Spring Integration creates a proxy implementation of the gateway interface.
- `defaultRequestChannel` specifies where gateway requests are sent.
- `@IntegrationComponentScan` discovers gateway interfaces and registers their proxies.
- Gateway method arguments can be mapped to message payloads and headers.
- `@Header` maps method parameters to message headers.
- A service activator can process gateway requests and return results.
- Spring Integration uses reply channels to deliver responses to waiting gateways.
- Gateway methods can provide request-response communication through ordinary Java method calls.
- Gateways reduce the need for application code to interact directly with message channels.
- Integration tests verify the complete gateway request-response flow.
- Unit tests verify business logic independently of messaging infrastructure.

---

## Next Example

The next example will explore **Channel Adapters** in Spring Integration, demonstrating how messaging flows can connect to external systems and how inbound and outbound channel adapters integrate external communication with Spring Integration message channels.
