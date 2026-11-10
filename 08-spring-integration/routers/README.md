# Routers

This example demonstrates **message routing** in Spring Integration.

It focuses on how Spring Integration routers examine incoming messages and determine which message channel should receive each message.

The example uses an order-processing scenario in which orders are routed to different destination channels based on their order type.

## What This Example Demonstrates

- What message routing means in Spring Integration.
- The role of routers in a messaging application.
- The `@Router` annotation.
- Routing messages based on their payload.
- Returning destination channel names from a router method.
- Using `DirectChannel` as an input and destination channel.
- Separating routing decisions from message handling.
- Publishing messages to a router's input channel.
- Recording messages received by destination channels.
- Testing message routing with Spring Boot integration tests.
- Testing unsupported message payload validation.

---

## What Is Message Routing?

Message routing is the process of determining where an incoming message should be delivered.

In a messaging application, different messages may need to be processed by different components depending on their content, type, or metadata.

A router makes this decision and directs each message to the appropriate destination channel.

For example, an order-processing application might need to handle retail and wholesale orders differently.

Instead of sending every order to the same processing component, a router can examine the order type and select the appropriate channel.

A simplified routing flow looks like:

```text
Incoming Message
       |
       v
   Input Channel
       |
       v
     Router
       |
       +-------------------+
       |                   |
       v                   v
 Retail Channel      Wholesale Channel
       |                   |
       v                   v
 Retail Handler      Wholesale Handler
```

The router determines the destination, while the downstream message handlers perform the actual processing.

---

## Spring Integration Routers

Spring Integration provides routers as message-handling components that determine which channel or channels should receive a message.

A router receives a message, evaluates its content or metadata, and selects one or more destination channels.

Spring Integration provides several ways to configure routing, including:

- Annotation-based routers.
- Expression-based routers.
- Header-based routers.
- Recipient list routers.
- Custom router implementations.

This example uses an annotation-based router with the `@Router` annotation.

---

## The `@Router` Annotation

The `@Router` annotation identifies a method as a message router.

For example:

```java
@Router(inputChannel = "orderInputChannel")
public String route(OrderMessage order) {
    return switch (order.orderType()) {
        
        case RETAIL -> "retailOrderChannel";
        
        case WHOLESALE -> "wholesaleOrderChannel";
    };
}
```

The annotation specifies the input channel from which the router receives messages.

The method examines the message payload and returns the name of the destination channel.

Spring Integration uses the returned channel name to determine where the message should be sent.

In this example:

- `RETAIL` orders are sent to `retailOrderChannel`.
- `WHOLESALE` orders are sent to `wholesaleOrderChannel`.

The router does not perform the actual order processing. Its responsibility is limited to selecting the destination.

---

## Order Message Model

The example uses an `OrderMessage` record to represent an incoming order.

```java
public record OrderMessage(long orderId, String product, int quantity, OrderType orderType) {
}
```

The `OrderType` enum defines the supported order categories:

```java
public enum OrderType {

    RETAIL,
    WHOLESALE

}
```

The order type determines which destination channel receives the message.

For example:

```java
OrderMessage retailOrder = new OrderMessage(1L, "Laptop", 2, OrderType.RETAIL);
OrderMessage wholesaleOrder = new OrderMessage(2L, "Keyboard", 50, OrderType.WHOLESALE);
```

The router uses the `orderType` property to make its routing decision.

Using an enum provides a clear representation of the supported order categories and allows the switch expression to handle all defined values.

---

## Order Message Router

The `OrderMessageRouter` class contains the routing logic.

```java
@MessageEndpoint
public class OrderMessageRouter {

    @Router(inputChannel = "orderInputChannel")
    public String route(OrderMessage order) {
        return switch (order.orderType()) {
            
            case RETAIL -> "retailOrderChannel";
            
            case WHOLESALE -> "wholesaleOrderChannel";
        };
    }

}
```

The class uses `@MessageEndpoint` to identify it as a Spring Integration message endpoint.

The `@Router` annotation registers the `route()` method as a router for messages arriving through `orderInputChannel`.

The method receives the `OrderMessage` payload rather than requiring application code to manually extract it from a `Message` object.

It then evaluates the order type and returns the corresponding destination channel name.

Conceptually:

```text
OrderMessage
     |
     v
  orderType
     |
     +--------------------+
     |                    |
     v                    v
  RETAIL              WHOLESALE
     |                    |
     v                    v
retailOrderChannel  wholesaleOrderChannel
```

The router therefore separates message destination selection from the components responsible for processing the orders.

---

## Input Channel

The example uses a `DirectChannel` as the router's input channel.

```java
@Bean
public MessageChannel orderInputChannel() {
    return new DirectChannel();
}
```

The `OrderMessagePublisher` sends messages to this channel.

The router subscribes to the channel through its `@Router` configuration.

The message flow begins when a message is published:

```text
OrderMessagePublisher
        |
        v
  orderInputChannel
        |
        v
  OrderMessageRouter
```

A `DirectChannel` provides point-to-point message delivery.

It invokes its subscriber synchronously in the sending thread, making it suitable for this small example.

---

## Destination Channels

The example defines two destination channels:

```java
@Bean
public MessageChannel retailOrderChannel(OrderRoutingSupport support) {
    DirectChannel channel = new DirectChannel();
    channel.subscribe(message -> support.record(message, "RETAIL"));
    return channel;
}

@Bean
public MessageChannel wholesaleOrderChannel(OrderRoutingSupport support) {
    DirectChannel channel = new DirectChannel();
    channel.subscribe(message -> support.record(message, "WHOLESALE"));
    return channel;
}
```

Each channel represents a different order destination.

### Retail Channel

The `retailOrderChannel` receives messages routed by the router when the order type is `RETAIL`.

Its subscriber records the received order with the destination `RETAIL`.

### Wholesale Channel

The `wholesaleOrderChannel` receives messages routed by the router when the order type is `WHOLESALE`.

Its subscriber records the received order with the destination `WHOLESALE`.

Both channels use `DirectChannel`, so their subscribers process messages synchronously.

For this example, the subscribers record the messages instead of implementing full retail or wholesale business-processing workflows.

---

## Message Publisher

The `OrderMessagePublisher` publishes orders to the router's input channel.

```java
@Component
public class OrderMessagePublisher {

    private final MessageChannel orderInputChannel;

    public OrderMessagePublisher(@Qualifier("orderInputChannel") MessageChannel orderInputChannel) {
        this.orderInputChannel = orderInputChannel;
    }

    public void publish(OrderMessage order) {
        orderInputChannel.send(MessageBuilder.withPayload(order).build());
    }

}
```

The publisher is responsible for creating and sending the message.

It does not need to know which destination channel will receive the order.

That decision belongs to the router.

This separation is important because publishers should not need to implement routing logic themselves.

The publisher only needs to know the input channel.

---

## Order Routing Support

The example uses `OrderRoutingSupport` to record the messages received by the destination channels.

```java
public class OrderRoutingSupport {

    private final List<RoutedOrder> routedOrders = new CopyOnWriteArrayList<>();

    public void record(Message<?> message, String destination) {
        if (!(message.getPayload() instanceof OrderMessage order)) {
            throw new IllegalArgumentException("Unsupported message payload");
        }

        routedOrders.add(new RoutedOrder(order, destination));
    }

    public List<RoutedOrder> getRoutedOrders() {
        return List.copyOf(routedOrders);
    }

    public void clear() {
        routedOrders.clear();
    }

    public record RoutedOrder(OrderMessage order, String destination) {
    }

}
```

The support class provides a simple way to observe the results of message routing.

It records:

- The original `OrderMessage`.
- The destination channel associated with the received message.

The `RoutedOrder` record represents this information.

For example:

```text
RoutedOrder
    |
    +-- OrderMessage
    |      |
    |      +-- orderId
    |      +-- product
    |      +-- quantity
    |      +-- orderType
    |
    +-- destination
```

The `CopyOnWriteArrayList` provides thread-safe storage for recorded messages.

The `getRoutedOrders()` method returns an immutable snapshot of the recorded orders.

The `clear()` method resets the recorded messages between tests.

### Payload Validation

The support class also validates the message payload:

```java
if (!(message.getPayload() instanceof OrderMessage order)) {
    throw new IllegalArgumentException("Unsupported message payload");
}
```

This ensures that only messages containing an `OrderMessage` are recorded.

If another payload type is received, the method throws an `IllegalArgumentException`.

This validation protects the support component from accidentally recording unexpected message types.

---

## Message Routing Flow

The complete message flow in this example looks like:

```text
OrderMessagePublisher
         |
         v
  orderInputChannel
         |
         v
  OrderMessageRouter
         |
         v
   Examine OrderType
         |
         +-----------------------+
         |                       |
         v                       v
      RETAIL                 WHOLESALE
         |                       |
         v                       v
retailOrderChannel    wholesaleOrderChannel
         |                       |
         v                       v
  Channel Subscriber     Channel Subscriber
         |                       |
         +-----------+-----------+
                     |
                     v
            OrderRoutingSupport
                     |
                     v
              RoutedOrder
```

The publisher sends an order without knowing its destination.

The router evaluates the order type and selects the appropriate channel.

The destination channel then delivers the message to its subscriber.

Finally, the support component records the routed order.

---

## Test Coverage

The example contains four tests.

| Test                                                           | Type        | Purpose                                          |
|----------------------------------------------------------------|-------------|--------------------------------------------------|
| `publishRetailOrder_shouldRouteMessageToRetailChannel`         | Integration | Verifies retail order routing.                   |
| `publishWholesaleOrder_shouldRouteMessageToWholesaleChannel`   | Integration | Verifies wholesale order routing.                |
| `publishMultipleOrders_shouldRouteEachMessageToItsDestination` | Integration | Verifies independent routing of multiple orders. |
| `record_shouldThrowExceptionForUnsupportedPayload`             | Unit        | Verifies unsupported payload validation.         |

The integration tests exercise the complete message flow:

```text
Publisher
    |
    v
Input Channel
    |
    v
Router
    |
    v
Destination Channel
    |
    v
Subscriber
```

The unit test focuses specifically on the support component's payload validation.

Together, these tests cover the main routing behavior and the defensive validation implemented by the example.

---

## Important Routing Details

### Router vs Message Handler

A router and a message handler have different responsibilities.

A message handler processes a message.

A router determines where a message should be delivered.

Conceptually:

```text
Message Handler
      |
      v
Processes Message
```

Whereas:

```text
Router
   |
   v
Examines Message
   |
   v
Selects Destination
   |
   v
Routes Message
```

A router can therefore direct messages to different handlers without implementing their business-processing logic.

### Router vs Transformer

A transformer changes a message's payload or structure.

A router determines the destination channel for a message.

| Component       | Responsibility                                         |
|-----------------|--------------------------------------------------------|
| Transformer     | Changes the message payload or structure.              |
| Router          | Determines the destination channel.                    |
| Filter          | Determines whether a message should be passed forward. |
| Message Handler | Processes a received message.                          |

These components can be combined to build more sophisticated messaging workflows.

### DirectChannel

This example uses `DirectChannel` for both input and destination channels.

A `DirectChannel` provides point-to-point message delivery and invokes its subscriber synchronously.

This means the message publisher's call to `send()` participates in the downstream message flow.

For this example, synchronous delivery makes it possible to inspect the recorded orders immediately after publishing them.

Other channel implementations, such as `QueueChannel` and `PublishSubscribeChannel`, provide different messaging behavior and can be explored separately.

### Destination Channel Names

The router returns channel names as strings:

```java
return switch (order.orderType()) {
    
    case RETAIL -> "retailOrderChannel";
    
    case WHOLESALE -> "wholesaleOrderChannel";
};
```

These names must correspond to channels registered in the Spring application context.

Spring Integration uses the returned destination name to resolve the channel.

An incorrectly configured or unavailable destination can cause routing to fail.

### Multiple Destinations

This example routes each order to exactly one destination channel.

Spring Integration also supports routing a message to multiple channels when the application requires that behavior.

For example, a message might be sent to both an audit channel and a notification channel.

That behavior is outside the scope of this basic example.

---

## Running the Example

From the repository root:

```bash
mvn -pl 08-spring-integration/routers -am spring-boot:run
```

The application starts with the router and its configured input and destination channels.

The example does not require an external message broker.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 08-spring-integration/routers -am clean verify
```

---

## Key Takeaways

- Message routing determines where incoming messages should be delivered.
- Spring Integration provides routers for directing messages between channels.
- The `@Router` annotation identifies a method as a message router.
- A router can examine a message payload and return a destination channel name.
- Spring Integration resolves the returned channel name and delivers the message.
- Routing decisions should be separated from downstream business processing.
- Publishers only need to know the router's input channel.
- Destination channels deliver messages to their subscribers.
- `DirectChannel` provides synchronous point-to-point message delivery.
- Integration tests can verify routing through the complete message flow.
- Unit tests can verify individual validation rules independently.

---

## Next Example

The next example will explore **Service Activators** in Spring Integration, demonstrating how messages are connected to application service methods for processing.
