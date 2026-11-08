# Message Channels

This example demonstrates **message channels** in Spring Integration.

It focuses on how message channels connect message producers and consumers, how messages are delivered to subscribers, and the differences between point-to-point message delivery and publish-subscribe message delivery.

The example uses two Spring Integration channel implementations:

- `DirectChannel`
- `PublishSubscribeChannel`

It also demonstrates how to configure these channels, publish messages through them, handle incoming messages, and test their behavior.

## What This Example Demonstrates

- What message channels are in Spring Integration.
- The relationship between message producers, channels, and message consumers.
- Spring Integration's `Message` abstraction.
- Message payloads and message construction with `MessageBuilder`.
- `DirectChannel` and point-to-point message delivery.
- How `DirectChannel` distributes messages across multiple subscribers.
- `PublishSubscribeChannel` and message broadcasting.
- How multiple subscribers receive the same message.
- Configuring message channels using Java configuration.
- Implementing message handlers with `MessageHandler`.
- Publishing messages through a service.
- Testing channel behavior and message delivery.
- Integration testing with Spring Boot.

---

## What Are Message Channels?

A message channel is a communication mechanism that connects components in a messaging application.

In Spring Integration, a producer sends a message to a channel, and one or more consumers receive that message.

The producer does not need to know which components consume the message. It only needs to know the channel to which the message should be sent.

A simplified messaging flow looks like:

```text
Message Producer
       |
       v
 Message Channel
       |
       v
Message Consumer
```

This separation allows application components to communicate without being tightly coupled to one another.

For example, an order service can publish an order message without knowing whether the message will be processed by an audit handler, a notification handler, or another component.

---

## The Spring Integration Message Abstraction

Spring Integration uses the `Message` abstraction to represent messages exchanged between components.

A message consists of two main parts:

- **Payload:** The actual data being transported.
- **Headers:** Metadata associated with the message.

Conceptually:

```text
          Message
       +---------------+
       |               |
       |    Headers    |
       |               |
       +---------------+
       |               |
       |    Payload    |
       |               |
       +---------------+
```

The payload can be an object, a string, a file, or another type of data.

The headers can contain metadata such as message IDs, timestamps, correlation IDs, and other information used during message processing.

### OrderMessage

This example uses an `OrderMessage` record as its message payload:

```java
public record OrderMessage(long orderId, String description) {}
```

An example order message looks like:

```java
OrderMessage order = new OrderMessage(1L, "First order");
```

The `OrderMessage` is the business data being transported through the channels.

It is wrapped in a Spring Integration `Message` before being sent.

---

## Creating Messages with MessageBuilder

Spring Integration provides `MessageBuilder` for constructing messages.

This example uses:

```java
MessageBuilder.withPayload(order).build()
```

The `withPayload()` method specifies the message payload, while `build()` creates the message.

For example:

```java
Message<OrderMessage> message = MessageBuilder.withPayload(order).build();
```

The resulting message contains the `OrderMessage` payload and the message headers managed by Spring Integration.

Using `MessageBuilder` makes it possible to construct messages with additional headers when required.

For example:

```java
Message<OrderMessage> message = MessageBuilder.withPayload(order).setHeader("source", "order-service").build();
```

The example keeps message construction simple because its primary focus is channel behavior.

---

## Message Channel Types

Spring Integration provides different channel implementations for different messaging requirements.

This example focuses on two commonly used implementations:

1. `DirectChannel`
2. `PublishSubscribeChannel`

They differ primarily in how messages are delivered to subscribers.

---

## DirectChannel

A `DirectChannel` provides point-to-point message delivery.

When a message is sent to a `DirectChannel`, it is delivered to one of its subscribed handlers rather than broadcast to every handler.

A simplified flow looks like:

```text
              Producer
                 |
                 v
           DirectChannel
                 |
          +------+------+
          |             |
          v             v
       Handler A     Handler B
```

Only one handler receives each message.

When multiple handlers subscribe to a `DirectChannel`, its default dispatching behavior distributes messages among them using round-robin selection.

For example:

```text
Message 1 ------> Handler A
Message 2 ------> Handler B
Message 3 ------> Handler A
Message 4 ------> Handler B
```

This provides a simple way to distribute work across multiple subscribers.

It is important to understand that a `DirectChannel` does not broadcast every message to all subscribers.

### Configuring a DirectChannel

The example configures a `DirectChannel` using Java configuration:

```java
@Bean
public DirectChannel orderChannel(DirectOrderMessageHandler handler) {
    DirectChannel channel = new DirectChannel();
    channel.subscribe(handler);
    return channel;
}
```

The configuration:

1. Creates a `DirectChannel`.
2. Subscribes the `DirectOrderMessageHandler` to the channel.
3. Exposes the channel as a Spring bean.

The application can then inject this channel wherever messages need to be published.

### DirectOrderMessageHandler

The `DirectOrderMessageHandler` implements Spring Integration's `MessageHandler` interface.

Its responsibility is to receive and process messages delivered through the direct channel.

Conceptually:

```text
OrderMessagePublisher
         |
         v
    DirectChannel
         |
         v
DirectOrderMessageHandler
         |
         v
MessageChannelSupport
```

The handler extracts the `OrderMessage` payload and records it through `MessageChannelSupport`.

---

## PublishSubscribeChannel

A `PublishSubscribeChannel` provides publish-subscribe message delivery.

Unlike a `DirectChannel`, it broadcasts a message to all subscribed handlers.

A simplified flow looks like:

```text
                 Producer
                    |
                    v
          PublishSubscribeChannel
                    |
          +---------+---------+
          |                   |
          v                   v
     Handler A             Handler B
          |                   |
          v                   v
      Processing          Processing
```

Every subscribed handler receives the same message.

For example:

```text
Order Event
     |
     v
PublishSubscribeChannel
     |
     +----------> Audit Handler
     |
     +----------> Notification Handler
```

This is useful when one event needs to trigger multiple independent actions.

An order event, for example, can be sent to an audit handler and a notification handler without the producer needing to know about either handler.

### Configuring a PublishSubscribeChannel

The example configures a `PublishSubscribeChannel` as follows:

```java
@Bean
public PublishSubscribeChannel orderEventsChannel(OrderAuditMessageHandler auditHandler,
        OrderNotificationMessageHandler notificationHandler) {

    PublishSubscribeChannel channel = new PublishSubscribeChannel();

    channel.subscribe(auditHandler);
    channel.subscribe(notificationHandler);

    return channel;
}
```

The configuration:

1. Creates a `PublishSubscribeChannel`.
2. Subscribes the `OrderAuditMessageHandler`.
3. Subscribes the `OrderNotificationMessageHandler`.
4. Exposes the channel as a Spring bean.

Whenever a message is sent to this channel, both handlers receive it.

### OrderAuditMessageHandler

The `OrderAuditMessageHandler` handles order events for auditing purposes.

It receives the order message and records it through `MessageChannelSupport`.

### OrderNotificationMessageHandler

The `OrderNotificationMessageHandler` handles order events for notification purposes.

It also receives the order message and records it through `MessageChannelSupport`.

The two handlers demonstrate how a single event can be consumed by multiple independent components.

---

## DirectChannel vs PublishSubscribeChannel

The main difference is how messages are distributed to subscribers.

### DirectChannel

```text
              Message
                 |
                 v
           DirectChannel
                 |
          +------+------+
          |             |
          v             v
       Handler A     Handler B

       One handler receives each message.
```

### PublishSubscribeChannel

```text
              Message
                 |
                 v
       PublishSubscribeChannel
                 |
          +------+------+
          |             |
          v             v
       Handler A     Handler B

       All handlers receive the message.
```

The following table summarizes the differences:

| Feature              | DirectChannel                          | PublishSubscribeChannel |
|----------------------|----------------------------------------|-------------------------|
| Delivery model       | Point-to-point                         | Publish-subscribe       |
| Message distribution | One subscriber per message             | All subscribers         |
| Multiple subscribers | Distributes messages among subscribers | Broadcasts messages     |
| Typical use          | Work distribution                      | Event broadcasting      |
| Default execution    | Synchronous                            | Synchronous             |

Both channels use synchronous dispatch by default in this example. A different execution model can be configured when asynchronous processing is required.

---

## Message Handlers

A message handler is a component responsible for processing messages received from a channel.

Spring Integration provides the `MessageHandler` interface:

```java
public interface MessageHandler {

    void handleMessage(Message<?> message);

}
```

A handler receives a message and performs the required processing.

This example implements three handlers:

| Handler                           | Responsibility                                         |
|-----------------------------------|--------------------------------------------------------|
| `DirectOrderMessageHandler`       | Processes messages received through the direct channel |
| `OrderAuditMessageHandler`        | Processes order events for auditing                    |
| `OrderNotificationMessageHandler` | Processes order events for notifications               |

Each handler implements `MessageHandler`.

This keeps message processing separate from message publishing.

---

## Publishing Messages

The `OrderMessagePublisher` service is responsible for publishing order messages through the configured channels.

It uses constructor injection to receive the two channels.

Conceptually:

```text
             OrderMessagePublisher
                       |
             +---------+---------+
             |                   |
             v                   v
        DirectChannel    PublishSubscribeChannel
             |                   |
             v                   v
      Direct Handler       Event Handlers
```

The service exposes two publishing operations:

- `publishOrder()` sends an order message to the `DirectChannel`.
- `publishOrderEvent()` sends an order message to the `PublishSubscribeChannel`.

The implementation follows this pattern:

```java
Message<OrderMessage> message = MessageBuilder.withPayload(order).build();

return orderChannel.send(message);
```

The `send()` method sends the message to the channel and returns a boolean indicating the result of the send operation.

For this example, the important distinction is which handlers receive the message after it is sent.

The publisher does not directly invoke the handlers.

Instead, it communicates through the channels.

---

## MessageChannelSupport

The example uses `MessageChannelSupport` to record messages received by the handlers.

It maintains separate collections for:

- Direct channel messages.
- Audit messages.
- Notification messages.

This makes it possible to verify which handlers received each message.

The support component also provides methods to retrieve the recorded messages and reset its state between tests.

It is included to make the example observable and testable without introducing external infrastructure such as a database or message broker.

---

## Message Delivery Flows

The example demonstrates two different message flows.

### Direct Order Message Flow

When `publishOrder()` is called:

```text
OrderMessage
     |
     v
OrderMessagePublisher
     |
     v
DirectChannel
     |
     v
DirectOrderMessageHandler
     |
     v
MessageChannelSupport
     |
     v
Direct Messages
```

The message is delivered to the direct handler.

The audit and notification handlers do not receive this message because they are subscribed to a different channel.

### Order Event Flow

When `publishOrderEvent()` is called:

```text
OrderMessage
     |
     v
OrderMessagePublisher
     |
     v
PublishSubscribeChannel
     |
     +-------------------+
     |                   |
     v                   v
Audit Handler      Notification Handler
     |                   |
     v                   v
MessageChannelSupport
     |                   |
     v                   v
Audit Messages     Notification Messages
```

Both handlers receive the same order message.

This demonstrates how publish-subscribe messaging allows multiple components to react independently to an event.

---

## Testing Message Channels

The example uses JUnit 5 and Spring Boot testing support.

The tests are divided into two categories:

- Channel unit tests.
- Application integration tests.

### DirectChannel Tests

`DirectChannelTest` verifies the behavior of a `DirectChannel`.

#### Single Subscriber

The first test verifies that a message sent to a `DirectChannel` is delivered to its subscriber.

It checks that:

- The message is successfully sent.
- The subscriber receives exactly one message.
- The received payload matches the original order.

#### Multiple Subscribers

The second test verifies message distribution across multiple subscribers.

It checks that:

- Two subscribers are registered.
- Two messages are sent.
- Each subscriber receives exactly one message.
- Both original messages are received exactly once across the subscribers.

This demonstrates the point-to-point distribution behavior of a `DirectChannel`.

### PublishSubscribeChannel Tests

`PublishSubscribeChannelTest` verifies the broadcasting behavior of a `PublishSubscribeChannel`.

#### Broadcasting to All Subscribers

The first test verifies that a message sent to the channel is delivered to every subscriber.

It checks that both subscribers receive the same payload.

#### Broadcasting Multiple Messages

The second test sends multiple messages to the channel.

It verifies that:

- Both subscribers receive every message.
- Each subscriber receives the expected number of messages.
- Both subscribers receive the messages in the expected order.

### Integration Tests

`OrderMessagePublisherIntegrationTest` uses `@SpringBootTest` to load the application context and test the configured channels and handlers together.

#### Publishing an Order

The first integration test calls:

```java
publisher.publishOrder(order);
```

It verifies that:

- The direct handler receives the order.
- The audit handler does not receive the order.
- The notification handler does not receive the order.

This confirms that the publisher sends the message through the direct channel.

#### Publishing an Order Event

The second integration test calls:

```java
publisher.publishOrderEvent(order);
```

It verifies that:

- The audit handler receives the order.
- The notification handler receives the order.
- The direct handler does not receive the order.

This confirms that the publish-subscribe channel broadcasts the message to both subscribers.

---

## Test Coverage Summary

The example contains six tests:

| Test Class                             | Number of Tests | Focus                                             |
|----------------------------------------|----------------:|---------------------------------------------------|
| `DirectChannelTest`                    |               2 | Point-to-point delivery and distribution          |
| `PublishSubscribeChannelTest`          |               2 | Broadcasting and multiple-message delivery        |
| `OrderMessagePublisherIntegrationTest` |               2 | Integration of publishers, channels, and handlers |
| **Total**                              |           **6** |                                                   |

The tests cover the essential behavior of both channel implementations and verify that the application configuration connects the components correctly.

---

## Important Testing Detail

The channel unit tests instantiate channel implementations directly:

```java
DirectChannel channel = new DirectChannel();
```

and:

```java
PublishSubscribeChannel channel = new PublishSubscribeChannel();
```

This allows their delivery behavior to be tested independently of the Spring application context.

The integration tests use:

```java
@SpringBootTest(classes = MessageChannelsApplication.class)
```

This loads the actual application configuration and injects the configured channels, handlers, and publisher.

The tests therefore demonstrate two complementary approaches:

```text
Channel Unit Tests
        |
        v
Test Individual Channel Behavior

Application Integration Tests
        |
        v
Test Configured Components Together
```

---

## Synchronous Message Processing

Both channel implementations in this example use their default synchronous dispatch behavior.

This means that when a message is sent, the subscribed handler or handlers are invoked as part of the sending operation.

Conceptually:

```text
Publisher
    |
    v
Channel.send(message)
    |
    v
Handler Processing
    |
    v
send() Returns
```

For a `DirectChannel`, the selected subscriber processes the message during the send operation.

For a `PublishSubscribeChannel`, all subscribed handlers are invoked during the send operation.

This makes the example straightforward to test because message processing completes before the publishing method returns.

Asynchronous processing can be configured separately when an application requires it.

---

## When Should You Use Each Channel?

### Use DirectChannel When:

- A message should be processed by one subscriber.
- Work should be distributed among multiple consumers.
- A producer should not depend directly on a particular handler.
- Synchronous in-process message processing is appropriate.

### Use PublishSubscribeChannel When:

- A message represents an event.
- Multiple independent components need to react to the same event.
- One producer should notify several consumers.
- Consumers should be able to subscribe without changing the producer.

For example, an order-created event might be consumed by:

- An audit component.
- A notification component.
- An analytics component.

A `PublishSubscribeChannel` provides a natural way to broadcast that event to all three components.

---

## Running the Example

From the repository root:

```bash
mvn -pl 08-spring-integration/message-channels spring-boot:run
```

The application starts with the configured message channels and handlers.

The example focuses on in-process messaging and does not require an external message broker.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 08-spring-integration/message-channels test
```

To run the complete Maven verification lifecycle:

```bash
mvn -pl 08-spring-integration/message-channels clean verify
```

---

## Key Takeaways

- A message channel connects message producers and consumers.
- Spring Integration represents messages using the `Message` abstraction.
- A message contains a payload and headers.
- `MessageBuilder` provides a convenient way to construct messages.
- `DirectChannel` provides point-to-point message delivery.
- A `DirectChannel` distributes messages among multiple subscribers using its default round-robin dispatching behavior.
- `PublishSubscribeChannel` broadcasts messages to all its subscribers.
- Message handlers implement the `MessageHandler` interface.
- Producers can publish messages without directly invoking consumers.
- Java configuration can be used to define and connect message channels.
- Synchronous channel dispatch makes message processing straightforward to test.
- Unit tests verify individual channel behavior, while integration tests verify the configured components together.

---

## Next Example

The next example will explore **Message Endpoints** in Spring Integration, including how endpoints connect message channels to application processing logic and how they participate in message-driven integration flows.
