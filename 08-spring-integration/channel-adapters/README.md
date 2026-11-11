# Channel Adapters

This example demonstrates **channel adapters** in Spring Integration.

It focuses on how Spring Integration connects external systems to messaging channels using inbound and outbound channel adapters.

The example uses file-based integration to demonstrate how files can enter a Spring Integration messaging flow, be processed, and then be written to an output directory.

## What This Example Demonstrates

- What channel adapters are in Spring Integration.
- The difference between inbound and outbound channel adapters.
- Inbound file adapters using `FileReadingMessageSource`.
- Outbound file adapters using `FileWritingMessageHandler`.
- Polling external resources for incoming messages.
- Publishing messages to Spring Integration channels.
- Processing files using a service activator.
- Writing processed messages to an output directory.
- Connecting inbound and outbound adapters through a messaging flow.
- Testing channel adapters using temporary directories.

---

## What Are Channel Adapters?

Channel adapters are components that connect external systems to Spring Integration messaging channels.

They provide a bridge between an external system and an application's messaging infrastructure.

Spring Integration provides two main types of channel adapters:

### Inbound Channel Adapters

An inbound channel adapter receives data from an external system or resource and publishes it as a message to a Spring Integration channel.

For example, an inbound file adapter can monitor a directory and publish messages when files become available.

Conceptually:

```text
External System
      │
      ▼
Inbound Channel Adapter
      │
      ▼
Message Channel
      │
      ▼
Message Consumer
```

The inbound adapter acts as the entry point into the messaging flow.

### Outbound Channel Adapters

An outbound channel adapter receives messages from a Spring Integration channel and sends their contents to an external system or resource.

For example, an outbound file adapter can receive a message and write its payload to a file.

Conceptually:

```text
Message Producer
      │
      ▼
Message Channel
      │
      ▼
Outbound Channel Adapter
      │
      ▼
External System
```

The outbound adapter acts as the exit point from the messaging flow.

---

## Channel Adapters vs Gateways

Channel adapters and gateways both connect external systems with Spring Integration messaging flows, but they serve different purposes.

### Channel Adapters

Channel adapters provide one-way communication.

An inbound adapter receives data and publishes it to a message channel.

An outbound adapter receives a message and sends it to an external destination.

```text
External System
      │
      ▼
Inbound Adapter
      │
      ▼
Messaging Flow
      │
      ▼
Outbound Adapter
      │
      ▼
External System
```

### Gateways

Gateways provide an interface for sending messages into a messaging flow and, when configured for request-response communication, receiving replies.

```text
Application
     │
     ▼
Gateway
     │
     ▼
Messaging Flow
     │
     ▼
Message Handler
     │
     ▼
Reply
     │
     ▼
Gateway
     │
     ▼
Application
```

A gateway is useful when application code needs to interact with a messaging flow through a method call.

A channel adapter is appropriate when connecting a messaging flow directly to an external system or resource.

---

## File-Based Integration

This example uses the Spring Integration file module to demonstrate channel adapters.

The application has two directories:

```text
data/
├── input/
└── output/
```

The input directory acts as an external source of files.

The output directory acts as the destination for processed files.

The application uses:

- An inbound file adapter to read files from the input directory.
- A service activator to process file contents.
- An outbound file adapter to write processed contents to the output directory.

The complete flow looks like:

```text
Input Directory
      │
      ▼
Inbound File Adapter
      │
      ▼
fileInputChannel
      │
      ▼
OrderFileProcessor
      │
      ▼
fileOutputChannel
      │
      ▼
Outbound File Adapter
      │
      ▼
Output Directory
```

---

## Inbound File Adapter

The inbound file adapter reads files from the input directory and publishes them to a Spring Integration message channel.

The example uses `FileReadingMessageSource` as its message source.

```java
@Bean
@InboundChannelAdapter(channel = "fileInputChannel", poller = @Poller(fixedDelay = "1000"))
public MessageSource<File> fileReadingMessageSource(@Value("${app.files.input-directory}") File inputDirectory) {
    FileReadingMessageSource source = new FileReadingMessageSource();
    source.setDirectory(inputDirectory);

    return source;
}
```

### `FileReadingMessageSource`

`FileReadingMessageSource` is a Spring Integration message source that reads files from a configured directory.

It produces messages whose payloads are `File` objects.

For example, if the input directory contains:

```text
data/input/
└── order.txt
```

The inbound adapter publishes a message conceptually equivalent to:

```java
Message<File>
```

The message payload contains the file that was discovered.

### `@InboundChannelAdapter`

The `@InboundChannelAdapter` annotation identifies a method that provides messages to a messaging channel.

```java
@InboundChannelAdapter(channel = "fileInputChannel", poller = @Poller(fixedDelay = "1000"))
```

The `channel` attribute specifies the destination channel.

In this example, incoming file messages are published to `fileInputChannel`.

### Polling

The adapter uses:

```java
@Poller(fixedDelay = "1000")
```

The poller periodically checks the message source for new files.

The `fixedDelay` value specifies the delay in milliseconds between polling attempts.

In this example, the adapter checks for available files approximately every second after the previous polling operation completes.

Polling allows the application to retrieve data from external resources that do not actively push messages into the messaging flow.

---

## Message Channels

The example defines two `DirectChannel` instances:

```java
@Bean
public MessageChannel fileInputChannel() {
    return new DirectChannel();
}

@Bean
public MessageChannel fileOutputChannel() {
    return new DirectChannel();
}
```

### `fileInputChannel`

The inbound adapter publishes discovered file messages to this channel.

The `OrderFileProcessor` subscribes to messages arriving through this channel.

### `fileOutputChannel`

The processor publishes transformed messages to this channel.

The outbound file adapter consumes messages from this channel and writes their payloads to files.

### Why `DirectChannel`?

A `DirectChannel` delivers messages synchronously to a subscribed message handler in the sending thread.

This is suitable for this small example because the processing flow does not require asynchronous execution or message buffering.

---

## Processing Incoming Files

The example uses `OrderFileProcessor` to process files received from the inbound adapter.

```java
@Component
public class OrderFileProcessor {

    @ServiceActivator(inputChannel = "fileInputChannel", outputChannel = "fileOutputChannel")
    public Message<String> process(File file) throws IOException {
        String content = Files.readString(file.toPath());
        String processedContent = content.toUpperCase(Locale.ROOT);

        return MessageBuilder.withPayload(processedContent).setHeader("file_name", "processed-" + file.getName()).build();
    }
}
```

The processor performs the following operations:

1. Receives a `File` payload from `fileInputChannel`.
2. Reads the file contents.
3. Converts the contents to uppercase.
4. Creates a new message containing the transformed content.
5. Adds a `file_name` header specifying the output filename.
6. Publishes the resulting message to `fileOutputChannel`.

For example, given an input file:

```text
order.txt
```

With the contents:

```text
order id: 1001, status: pending
```

The processor produces a message with:

```text
Payload:
ORDER ID: 1001, STATUS: PENDING

Header:
file_name = processed-order.txt
```

### `@ServiceActivator`

The `@ServiceActivator` annotation connects a method to a messaging channel.

```java
@ServiceActivator(inputChannel = "fileInputChannel", outputChannel = "fileOutputChannel")
```

The `inputChannel` identifies where the processor receives messages.

The `outputChannel` identifies where the method's return value is sent.

Because the method returns a message, Spring Integration forwards that message to `fileOutputChannel`.

This allows the processor to participate in the messaging flow without manually sending messages to the output channel.

---

## Outbound File Adapter

The outbound file adapter receives processed messages and writes their payloads to the output directory.

The example uses `FileWritingMessageHandler`.

```java
@Bean
@ServiceActivator(inputChannel = "fileOutputChannel")
public MessageHandler fileWritingMessageHandler(@Value("${app.files.output-directory}") File outputDirectory) {
    FileWritingMessageHandler handler = new FileWritingMessageHandler(outputDirectory);
    handler.setExpectReply(false);
    handler.setFileNameGenerator(message -> (String) message.getHeaders().get("file_name"));

    return handler;
}
```

### `FileWritingMessageHandler`

`FileWritingMessageHandler` is a Spring Integration message handler that writes message payloads to files.

It receives messages from `fileOutputChannel` and writes their contents to the configured output directory.

### `@ServiceActivator`

The annotation connects the outbound message handler to the output channel:

```java
@ServiceActivator(inputChannel = "fileOutputChannel")
```

When the processor publishes a message to this channel, the outbound adapter receives it.

### `setExpectReply(false)`

The example configures:

```java
handler.setExpectReply(false);
```

This indicates that the outbound handler does not need to produce a reply message.

The file-writing operation is an outbound operation, so the example does not require request-response behavior.

### File Name Generation

The example uses a custom file name generator:

```java
handler.setFileNameGenerator(message -> (String) message.getHeaders().get("file_name"));
```

The generator obtains the output filename from the message's `file_name` header.

This allows the processor to determine the name of the file created by the outbound adapter.

For example:

```text
Input:
order.txt

Output:
processed-order.txt
```

---

## Complete Channel Adapter Configuration

The channel adapter configuration brings the inbound and outbound adapters together.

```java
@Configuration
public class ChannelAdapterConfiguration {

    @Bean
    public MessageChannel fileInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel fileOutputChannel() {
        return new DirectChannel();
    }

    @Bean
    @InboundChannelAdapter(channel = "fileInputChannel", poller = @Poller(fixedDelay = "1000"))
    public MessageSource<File> fileReadingMessageSource(@Value("${app.files.input-directory}") File inputDirectory) {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(inputDirectory);

        return source;
    }

    @Bean
    @ServiceActivator(inputChannel = "fileOutputChannel")
    public MessageHandler fileWritingMessageHandler(@Value("${app.files.output-directory}") File outputDirectory) {
        FileWritingMessageHandler handler = new FileWritingMessageHandler(outputDirectory);
        handler.setExpectReply(false);
        handler.setFileNameGenerator(message -> (String) message.getHeaders().get("file_name"));

        return handler;
    }
}
```

The configuration establishes the following connections:

```text
FileReadingMessageSource
          │
          ▼
   fileInputChannel
          │
          ▼
   OrderFileProcessor
          │
          ▼
  fileOutputChannel
          │
          ▼
FileWritingMessageHandler
```

The inbound and outbound adapters do not need to know about each other.

They communicate through the messaging channels, while the processor handles the transformation between them.

---

## File Processing Example

Suppose the input directory contains:

```text
data/input/
└── order.txt
```

The file contains:

```text
order id: 1001, status: pending
```

The inbound adapter discovers the file and publishes it to `fileInputChannel`.

The processor reads the contents and transforms them:

```text
ORDER ID: 1001, STATUS: PENDING
```

The processor then publishes the transformed message to `fileOutputChannel`.

The outbound adapter writes the message to:

```text
data/output/
└── processed-order.txt
```

The resulting output file contains:

```text
ORDER ID: 1001, STATUS: PENDING
```

The complete operation demonstrates how an external file source can be connected to an external file destination through Spring Integration messaging channels.

---

## Important Implementation Details

### Inbound Adapter Polling

The inbound adapter uses a poller to periodically check the input directory.

Polling is useful when the external resource does not actively publish messages.

The configured one-second delay is for demonstration purposes and does not guarantee that every file will be processed exactly one second after it is created.

### File Acceptance

`FileReadingMessageSource` uses an accept-once filter by default.

This helps prevent the same file from being repeatedly picked up during the application's lifetime.

However, the default in-memory filter does not provide durable tracking across application restarts.

Applications requiring durable file tracking should consider a persistent file list filter.

### Message Payload and Headers

The example uses two different parts of a Spring Messaging message:

- **Payload:** contains the actual processed file contents.
- **Headers:** contain additional message metadata, such as the output filename.

Conceptually:

```text
Message
  │
  ├── Payload
  │     └── ORDER ID: 1001, STATUS: PENDING
  │
  └── Headers
        └── file_name = processed-order.txt
```

Separating the content from its metadata makes it possible to control output file naming without embedding filenames into the message payload.

### Synchronous Processing

The example uses `DirectChannel` instances.

Message delivery to their subscribed handlers occurs synchronously.

Consequently, when the inbound adapter publishes a message, processing and the outbound file-writing operation execute as part of that message delivery flow.

---

## Running the Example

From the repository root:

```bash
mvn -pl 08-spring-integration/channel-adapters spring-boot:run
```

The application starts with the following directories configured:

```text
data/
├── input/
└── output/
```

Place a text file in the input directory.

For example:

```text
data/input/order.txt
```

With contents:

```text
order id: 1001, status: pending
```

The inbound adapter polls the directory, and the application processes the file.

The resulting file is written to:

```text
data/output/processed-order.txt
```

---

## Running the Tests

Run the tests from the module:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 08-spring-integration/channel-adapters -am clean verify
```

---

## Key Takeaways

- Channel adapters connect external systems and resources to Spring Integration messaging channels.
- Inbound channel adapters receive external data and publish messages into a messaging flow.
- Outbound channel adapters receive messages and send data to external destinations.
- `FileReadingMessageSource` provides an inbound file message source.
- `FileWritingMessageHandler` provides an outbound file message handler.
- `@InboundChannelAdapter` configures an inbound message source.
- Pollers allow inbound adapters to periodically retrieve data from external resources.
- `@ServiceActivator` connects message-processing methods and handlers to messaging channels.
- `DirectChannel` provides synchronous message delivery.
- Message payloads contain the actual data, while headers carry additional metadata.
- Channel adapters provide one-way communication, unlike request-response gateways.
- Temporary directories help isolate file-based integration tests.

---

## Next Example

The next example will explore **Error Channels** in Spring Integration, including how messaging errors are routed, how error messages are handled, and how applications can implement centralized error handling for messaging flows.
