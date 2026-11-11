# Error Channels

This example demonstrates **error handling with error channels** in Spring Integration.

It focuses on how Spring Integration handles exceptions during message processing, how error messages are published to dedicated error channels, and how applications can process those errors independently of the main messaging flow.

The example uses asynchronous message processing, a dedicated error channel, and an error handler to demonstrate how exceptions are propagated and handled in a Spring Integration application.

## What This Example Demonstrates

- What error channels are in Spring Integration.
- How Spring Integration handles exceptions during message processing.
- The relationship between message channels and error channels.
- The `ErrorMessage` abstraction.
- The `ErrorChannel` message header.
- Asynchronous message processing with `ExecutorChannel`.
- Publishing errors to a dedicated error channel.
- Handling errors with `@ServiceActivator`.
- Extracting the original failed message from an error message.
- Understanding `MessagingException` and its failed message.
- Extracting the root cause of an exception.
- Testing error handling with integration and unit tests.

---

## What Are Error Channels?

An error channel is a message channel used to deliver error messages generated during message processing.

In a typical Spring Integration application, messages move through channels and message-handling components.

If a component throws an exception while processing a message, Spring Integration can publish an error message to an error channel.

This allows an application to handle failures separately from its normal message-processing flow.

A simplified flow looks like:

```text
Incoming Message
       |
       v
 Message Channel
       |
       v
 Message Handler
       |
       v
 Message Processing
       |
       +--------------- Success
       |                    |
       |                    v
       |                Continue
       |
       +--------------- Failure
                            |
                            v
                       Error Message
                            |
                            v
                       Error Channel
                            |
                            v
                       Error Handler
```

Instead of allowing an exception to remain unhandled, the application can route the failure to a component responsible for processing error messages.

---

## Understanding ErrorMessage

Spring Integration uses Spring Messaging's `ErrorMessage` class to represent an error as a message.

An `ErrorMessage` contains:

- A `Throwable` as its payload.
- An optional reference to the original message that caused the failure.

Conceptually:

```text
ErrorMessage
     |
     +-- Payload
     |     |
     |     v
     |   Throwable
     |
     +-- Original Message
              |
              v
          Failed Payload
```

The exception provides information about the failure, while the original message provides context about what was being processed.

For example, an order-processing failure might produce an error message containing:

```text
ErrorMessage
    |
    +-- Exception
    |     |
    |     v
    |   IllegalArgumentException
    |
    +-- Original Message
          |
          v
      OrderMessage
        orderId: ORDER-001
        quantity: 0
```

This information allows the error handler to identify both the failure and the message that caused it.

---

## The Default Error Channel

Spring Integration provides a global error channel named `errorChannel`.

It is used as the default destination for error messages when no more specific error channel has been configured.

Conceptually:

```text
Message Processing Failure
          |
          v
    Error Message
          |
          v
    errorChannel
          |
          v
    Error Handler
```

An application can define its own error channel when different parts of the messaging application require separate error-handling strategies.

A dedicated error channel makes it possible to isolate failures and handle them according to the requirements of a particular messaging flow.

---

## Dedicated Error Channels

This example defines a dedicated error channel named `orderErrorChannel`.

The channel is responsible for receiving errors generated while processing orders.

The configuration defines the channel as follows:

```java
@Bean
public MessageChannel orderErrorChannel() {
    return new DirectChannel();
}
```

A `DirectChannel` delivers messages to its subscribed handler in the sender's thread.

In this example, the error channel is used to dispatch error messages to a dedicated error handler.

The distinction between the asynchronous order-processing channel and the error channel is important:

| Channel             | Purpose                                              |
|---------------------|------------------------------------------------------|
| `orderInputChannel` | Receives order messages for asynchronous processing. |
| `orderErrorChannel` | Receives errors generated during order processing.   |

The error channel does not replace the original input channel. It provides a separate destination for failures.

---

## Asynchronous Message Processing

The example uses an `ExecutorChannel` for order processing.

An `ExecutorChannel` dispatches messages through a task executor, allowing message handling to happen asynchronously.

The configuration is:

```java
@Bean
public ThreadPoolTaskExecutor orderTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(2);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("order-");
    
    return executor;
}

@Bean
public MessageChannel orderInputChannel(ThreadPoolTaskExecutor orderTaskExecutor) {
    return new ExecutorChannel(orderTaskExecutor);
}
```

The task executor controls the threads used to process messages.

The configured executor has:

- A core pool size of 2.
- A maximum pool size of 2.
- A queue capacity of 100.
- A thread name prefix of `order-`.

This allows multiple order-processing tasks to be executed independently of the thread that publishes the messages.

### DirectChannel vs ExecutorChannel

These channels have different message-dispatching behaviour.

**DirectChannel**

```text
Publisher Thread
       |
       v
 DirectChannel
       |
       v
 Message Handler
       |
       v
 Same Thread
```

The message handler executes in the sender's thread.

**ExecutorChannel**

```text
Publisher Thread
       |
       v
 ExecutorChannel
       |
       v
 Task Executor
       |
       v
 Worker Thread
       |
       v
 Message Handler
```

The message handler executes through a task executor.

This distinction matters when handling exceptions because an exception thrown during asynchronous processing cannot simply propagate back to the original publishing thread.

Spring Integration uses error-message publishing to make such failures available to error-handling components.

---

## Publishing Messages to an Error Channel

The example's publisher sends order messages to `orderInputChannel`.

It also specifies the dedicated error channel through the standard `errorChannel` message header.

The relevant code is:

```java
Message<OrderMessage> message = MessageBuilder.withPayload(order)
        .setHeader(MessageHeaders.ERROR_CHANNEL, orderErrorChannel).build();

return orderInputChannel.send(message);
```

The `MessageHeaders.ERROR_CHANNEL` header identifies where an error message should be published if processing fails.

This is particularly useful with asynchronous channels because the publisher does not directly receive exceptions thrown by the message handler.

The message carries the error destination along with its payload.

Conceptually:

```text
OrderMessage
     |
     v
MessageBuilder
     |
     +-- Payload: OrderMessage
     |
     +-- Header: errorChannel
     |       |
     |       v
     |   orderErrorChannel
     |
     v
orderInputChannel
```

When processing fails, Spring Integration can use this header to determine where to publish the resulting error message.

The header is part of the message metadata; it does not change the order payload.

---

## Processing Orders

The example contains an order-processing service that validates incoming orders.

The service uses `@ServiceActivator` to subscribe to `orderInputChannel`.

```java
@Service
public class OrderProcessingService {

    @ServiceActivator(inputChannel = "orderInputChannel")
    public void process(OrderMessage order) {
        if (order.quantity() <= 0) {
            throw new IllegalArgumentException("Order quantity must be greater than zero");
        }
    }
}
```

The service rejects orders with a quantity less than or equal to zero.

For example:

```java
new OrderMessage("ORDER-001", 0);
```

causes an `IllegalArgumentException`.

Because the input channel is asynchronous, the exception occurs during message processing on an executor thread.

The error-handling mechanism can then publish an `ErrorMessage` to `orderErrorChannel`.

---

## Handling Error Messages

The example defines an error handler that subscribes to `orderErrorChannel`.

It uses `@ServiceActivator` to receive error messages.

```java
@Component
public class ErrorMessageHandler {

    private final ErrorProcessingSupport errorProcessingSupport;

    @ServiceActivator(inputChannel = "orderErrorChannel")
    public void handle(ErrorMessage errorMessage) {
        
        ErrorDetails details = errorProcessingSupport.process(errorMessage);

        // Process or record the error details.
    }
}
```

The handler receives an `ErrorMessage` rather than the original order message.

It delegates error extraction to `ErrorProcessingSupport`.

Separating error extraction from message handling keeps the handler focused on receiving and processing errors.

In a production application, this handler could be extended to:

- Log processing failures.
- Store failed messages for investigation.
- Notify an external monitoring system.
- Publish errors to another messaging destination.
- Trigger a retry or recovery workflow.

These are possible extensions rather than behaviours implemented by this example.

---

## Understanding MessagingException

An important detail when processing error messages is that the exception payload is not necessarily the original exception thrown by the application.

Spring Integration may wrap an application exception in a `MessagingException`, such as a `MessageHandlingException`.

A `MessagingException` provides access to the message associated with the failure through `getFailedMessage()`.

For example:

```text
ErrorMessage
     |
     v
MessageHandlingException
     |
     +-- Failed Message
     |       |
     |       v
     |   OrderMessage
     |
     +-- Cause
             |
             v
       IllegalArgumentException
```

This is important because the error handler may need to inspect the failed message to determine which order caused the exception.

The example's `ErrorProcessingSupport` checks the original message associated with the `ErrorMessage` and can also retrieve the failed message from a `MessagingException`.

This makes error extraction more reliable when Spring Integration wraps exceptions during message processing.

---

## Extracting the Root Cause

Exception wrapping can produce multiple levels of exceptions.

For example:

```text
MessageHandlingException
        |
        v
IllegalStateException
        |
        v
IllegalArgumentException
```

The root cause is the deepest underlying exception.

In this example, the original validation failure is an `IllegalArgumentException`.

The support component unwraps the exception chain to obtain the root cause.

Conceptually:

```text
Exception
    |
    v
Check Cause
    |
    +-- Cause Exists
    |       |
    |       v
    |   Check Next Cause
    |
    +-- No Cause
            |
            v
       Root Cause
```

This allows the error handler to report the underlying application failure instead of only reporting a Spring messaging wrapper.

For example, the resulting error details contain:

| Property          | Value                                      |
|-------------------|--------------------------------------------|
| Failed order      | `ORDER-001`                                |
| Exception type    | `IllegalArgumentException`                 |
| Exception message | `Order quantity must be greater than zero` |

---

## Error Details

The example uses an `ErrorDetails` record to represent the information extracted from an error message.

Conceptually:

```java
public record ErrorDetails(OrderMessage failedOrder, String exceptionType, String exceptionMessage) {
}
```

This provides a simple representation of the failed order and its underlying exception.

Instead of requiring other application components to understand Spring Integration's `ErrorMessage` and `MessagingException` structures, they can work with a straightforward error-details object.

This also makes the error-processing logic easier to test independently.

---

## Error Processing Flow

The complete flow for a successful order looks like:

```text
OrderPublisher
      |
      v
OrderMessage
      |
      v
orderInputChannel
      |
      v
ExecutorChannel
      |
      v
OrderProcessingService
      |
      v
Order Validation
      |
      v
Successful Processing
```

The failure flow looks like:

```text
OrderPublisher
      |
      v
OrderMessage
      |
      v
orderInputChannel
      |
      v
ExecutorChannel
      |
      v
OrderProcessingService
      |
      v
Order Validation
      |
      v
Exception Thrown
      |
      v
Error Message Created
      |
      v
orderErrorChannel
      |
      v
ErrorMessageHandler
      |
      v
ErrorProcessingSupport
      |
      v
ErrorDetails
```

The important distinction is that the normal processing flow and error-handling flow have separate responsibilities.

---

## Running the Example

From the repository root:

```bash
mvn -pl 08-spring-integration/error-channels spring-boot:run
```

The application starts with the configured asynchronous order-processing flow and dedicated error channel.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 08-spring-integration/error-channels -am test
```

To run the complete verification lifecycle, including compilation, tests, and verification checks:

```bash
mvn -pl 08-spring-integration/error-channels -am clean verify
```

---

## Key Takeaways

- Error channels provide a dedicated destination for errors generated during message processing.
- Spring Integration represents errors using the `ErrorMessage` abstraction.
- The default global error channel is named `errorChannel`.
- Applications can define dedicated error channels for individual messaging flows.
- The `MessageHeaders.ERROR_CHANNEL` header identifies a message-specific error destination.
- `ExecutorChannel` dispatches messages asynchronously through a task executor.
- Exceptions thrown during asynchronous processing cannot simply propagate to the original publishing thread.
- Spring Integration can publish errors to the configured error channel.
- `@ServiceActivator` can be used to handle messages arriving on an error channel.
- `MessagingException.getFailedMessage()` provides access to the message associated with a messaging failure.
- Exception wrapping means the error payload may not be the original application exception.
- Extracting the root cause helps identify the underlying application failure.
- Unit tests and integration tests verify different aspects of error handling.

---

## Next Example

The next example will explore **Integration Testing** in Spring Integration, including how to test integration flows, verify message delivery between channels, and validate the behavior of messaging components in an application.
