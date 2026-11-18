# Distributed Tracing

This example demonstrates **distributed tracing** in Spring Boot applications using Spring Boot's observability support, Micrometer Tracing, and OpenTelemetry.

It focuses on how trace context is created, propagated, and maintained across multiple Spring Boot services communicating over HTTP.

The example consists of an Order Service and an Inventory Service. When an order is submitted, the Order Service calls the Inventory Service to check product availability. Distributed tracing makes it possible to follow this interaction as part of a single trace.

## What This Example Demonstrates

- What distributed tracing means in a microservices architecture.
- Traces, spans, trace IDs, and span IDs.
- Distributed trace context propagation across HTTP requests.
- Spring Boot observability and Micrometer Observation.
- Custom observations using `ObservationRegistry`.
- Accessing the current trace using Micrometer Tracing.
- Automatic HTTP instrumentation with `RestClient`.
- OpenTelemetry trace exporting.
- Visualizing distributed traces with Jaeger.
- Testing distributed trace propagation across services.
- Testing HTTP-level application behavior.

---

## What Is Distributed Tracing?

Distributed tracing is a technique used to observe and understand requests as they travel through multiple services in a distributed application.

In a microservices architecture, a single user request may involve several independent services.

For example, placing an order might require:

1. Receiving an order request.
2. Processing the order.
3. Calling the Inventory Service.
4. Checking product availability.
5. Returning the result to the Order Service.
6. Returning the final response to the client.

Without distributed tracing, identifying how a request moved between services and where a failure or performance bottleneck occurred can be difficult.

Distributed tracing provides a way to connect these operations using a shared trace identifier.

A simplified flow looks like:

```text
Client
  │
  ▼
Order Service
  │
  ├── Process Order
  │
  ▼
Inventory Service
  │
  ├── Check Availability
  │
  ▼
Order Service
  │
  ▼
Client
```

Each operation can contribute spans to the same distributed trace.

---

## Example Architecture

This example contains two independent Spring Boot applications.

```text
                 HTTP POST /api/orders
                           │
                           ▼
              ┌────────────────────────┐
              │     Order Service      │
              │                        │
              │  - OrderController     │
              │  - OrderService        │
              │  - RestClient          │
              └────────────┬───────────┘
                           │
                           │ HTTP GET
                           │ Trace Context
                           ▼
              ┌────────────────────────┐
              │   Inventory Service   │
              │                        │
              │  - InventoryController│
              │  - InventoryService    │
              └────────────────────────┘

                           │
                           ▼
                  OpenTelemetry
                           │
                           ▼
                        Jaeger
```

The Order Service receives an order request and communicates with the Inventory Service to determine whether the requested quantity is available.

Both services participate in the same distributed trace.

### Modules

The example is organized into three Maven modules:

| Module                 | Responsibility                                         |
|------------------------|--------------------------------------------------------|
| `dt-order-service`     | Receives and processes order requests.                 |
| `dt-inventory-service` | Provides product availability information.             |
| `dt-integration-tests` | Contains unit and integration tests for both services. |

The services run independently and communicate through HTTP.

---

## What Is a Trace?

A trace represents the complete journey of a request through a distributed system.

It contains one or more spans that describe individual operations performed while processing that request.

For example:

```text
Trace: 0123456789abcdef0123456789abcdef
  │
  ├── Order Service
  │     ├── HTTP POST /api/orders
  │     └── order.process
  │
  └── Inventory Service
        ├── HTTP GET /api/inventory/SKU-1001
        └── inventory.lookup
```

The actual spans and their hierarchy depend on the instrumentation and observations configured in the application.

A trace is identified by a unique trace ID that is shared by its spans.

This makes it possible to correlate operations across different services.

---

## What Is a Span?

A span represents an individual operation within a trace.

Examples include:

- Handling an incoming HTTP request.
- Processing an order.
- Making an HTTP request to another service.
- Looking up inventory information.

A span typically contains:

- A trace ID.
- A span ID.
- A parent span ID, when applicable.
- An operation name.
- Start and end timestamps.
- Duration.
- Attributes describing the operation.
- Events and status information.

A simplified span structure looks like:

```text
Trace
  │
  ├── Span A: HTTP POST /api/orders
  │      │
  │      └── Span B: order.process
  │              │
  │              └── Span C: HTTP GET /api/inventory
  │
  └── Span D: inventory.lookup
```

Spans are connected through parent-child relationships to describe how operations relate to one another.

---

## Trace ID vs Span ID

Although both identifiers are associated with tracing, they serve different purposes.

### Trace ID

The trace ID identifies an entire distributed request.

All spans belonging to the same trace share the same trace ID.

### Span ID

The span ID identifies an individual operation within a trace.

Each span has its own span ID.

For example:

| Identifier | Value                              | Purpose                                    |
|------------|------------------------------------|--------------------------------------------|
| Trace ID   | `0123456789abcdef0123456789abcdef` | Identifies the complete distributed trace. |
| Span ID    | `0123456789abcdef`                 | Identifies an individual operation.        |

Conceptually:

```text
Trace ID: TRACE-001
    │
    ├── Span ID: SPAN-001
    │
    ├── Span ID: SPAN-002
    │
    └── Span ID: SPAN-003
```

The trace ID connects the operations, while the span IDs distinguish them.

---

## Distributed Trace Context Propagation

Trace context propagation is the mechanism that allows tracing information to travel between services.

When the Order Service receives an HTTP request, tracing instrumentation creates or continues a trace.

When it calls the Inventory Service, the tracing information is propagated through HTTP headers.

The receiving service extracts that context and continues the same trace.

A simplified flow looks like:

```text
Client
  │
  │ HTTP Request
  ▼
Order Service
  │
  │ Trace Context Created
  │
  │ HTTP Request + Trace Headers
  ▼
Inventory Service
  │
  │ Trace Context Extracted
  │
  │ Continues Existing Trace
  ▼
Response
```

This allows operations performed by independent services to be correlated.

### W3C Trace Context

The W3C Trace Context specification defines a standard format for propagating tracing information between distributed systems.

The primary header is:

```http
traceparent: 00-0123456789abcdef0123456789abcdef-0123456789abcdef-01
```

It contains:

- Version.
- Trace ID.
- Parent ID.
- Trace flags.

The `traceparent` header allows different tracing implementations to exchange trace context using a common format.

Spring Boot's tracing instrumentation supports context propagation through supported HTTP clients and servers.

---

## Spring Boot Observability

Spring Boot provides observability support through Micrometer.

Observability makes it possible to understand an application's internal behavior through:

- Metrics.
- Logs.
- Traces.

These signals provide different perspectives on application behavior.

### Metrics

Metrics represent numerical measurements collected over time.

Examples include:

- HTTP request count.
- Request duration.
- JVM memory usage.
- Database connection usage.

### Logs

Logs record events that occur while an application is running.

They provide information about application execution, errors, and operational events.

### Traces

Traces describe the journey of a request through an application or distributed system.

They help identify where time is spent and how services interact.

These three observability signals complement one another.

---

## Micrometer Observation

Spring Boot uses Micrometer Observation to instrument application operations.

An observation represents an operation that can produce one or more observability signals, depending on the configured observation handlers.

In this example, custom observations are used to instrument business operations.

For example, the Order Service creates an observation around order processing:

```java
observationRegistry.observationConfig();

Observation.createNotStarted("order.process", observationRegistry)
        .observe(() -> {
            // Process order
        });
```

The Inventory Service similarly instruments inventory lookup:

```java
Observation.createNotStarted("inventory.lookup", observationRegistry)
        .observe(() -> {
            // Look up inventory
        });
```

These observations provide visibility into application-level operations rather than relying exclusively on HTTP instrumentation.

### Why Use Custom Observations?

Automatic instrumentation can capture HTTP requests and other supported operations.

However, it does not necessarily describe the business operations that matter to an application.

Custom observations allow developers to instrument meaningful operations such as:

- Processing an order.
- Reserving inventory.
- Calculating a payment.
- Confirming a transaction.

This provides more useful application-level visibility.

---

## Micrometer Tracing

Micrometer Tracing provides tracing abstractions that allow applications to work with traces and spans without directly depending on a specific tracing implementation.

The `Tracer` interface provides access to the current span and its associated trace context.

For example:

```java
private final Tracer tracer;

public String getCurrentTraceId() {
    Span currentSpan = tracer.currentSpan();

    return currentSpan != null ? currentSpan.context().traceId() : null;
}
```

The current span represents the active operation in the current execution context.

The trace ID can be obtained from its context.

This example uses the current trace ID to make trace propagation visible in the service responses.

---

## Automatic HTTP Instrumentation

Spring Boot provides observability instrumentation for supported HTTP clients.

This example uses Spring's `RestClient` to communicate between services.

The Order Service configures its client using the auto-configured `RestClient.Builder`:

```java
@Bean
public RestClient inventoryRestClient(RestClient.Builder builder, @Value("${inventory.service.url}") String inventoryServiceUrl) {
    return builder.baseUrl(inventoryServiceUrl).build();
}
```

An important detail is that the application uses the injected `RestClient.Builder` rather than creating a standalone builder with `RestClient.builder()`.

The auto-configured builder can carry Spring Boot's HTTP client customizations and observability instrumentation.

The Order Service then calls the Inventory Service:

```java
InventoryResponse inventory = inventoryRestClient.get().uri("/api/inventory/{productId}", request.productId()).retrieve()
        .body(InventoryResponse.class);
```

When tracing instrumentation is configured, the HTTP client observation participates in the active trace and propagates trace context to the downstream service.

The Inventory Service can then continue the trace when it receives the request.

---

## Order Service

The Order Service is responsible for receiving and processing order requests.

It exposes:

```http
POST /api/orders
```

An example request is:

```json
{
  "orderId": "ORD-1001",
  "productId": "SKU-1001",
  "quantity": 2
}
```

The service:

1. Receives and validates the order request.
2. Starts an order-processing observation.
3. Retrieves the current trace ID.
4. Calls the Inventory Service.
5. Compares the requested quantity with available inventory.
6. Returns an order response.

An accepted response looks like:

```json
{
  "orderId": "ORD-1001",
  "productId": "SKU-1001",
  "quantity": 2,
  "availableQuantity": 25,
  "status": "ACCEPTED",
  "traceId": "0123456789abcdef0123456789abcdef",
  "inventoryTraceId": "0123456789abcdef0123456789abcdef"
}
```

The response includes both the Order Service trace ID and the trace ID returned by the Inventory Service.

This makes it possible to verify that both services participated in the same distributed trace.

---

## Inventory Service

The Inventory Service provides product availability information.

It exposes:

```http
GET /api/inventory/{productId}
```

For example:

```http
GET /api/inventory/SKU-1001
```

The service performs an inventory lookup and returns the available quantity.

Example response:

```json
{
  "productId": "SKU-1001",
  "availableQuantity": 25,
  "traceId": "0123456789abcdef0123456789abcdef"
}
```

The service also creates a custom observation around inventory lookup.

Because the incoming HTTP request carries the trace context, the Inventory Service can participate in the trace initiated by the Order Service.

---

## Application Configuration

Both services enable tracing through Spring Boot's observability configuration.

An example configuration is:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

  tracing:
    sampling:
      probability: 1.0

  opentelemetry:
    tracing:
      export:
        otlp:
          endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4318/v1/traces}

logging:
  pattern:
    correlation: "[${spring.application.name:},%X{traceId:-},%X{spanId:-}] "
```

### Sampling Probability

The sampling probability controls the proportion of traces that are sampled.

For example:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
```

A probability of `1.0` means that all traces are sampled.

In production environments, sampling is often configured according to application traffic, observability requirements, and resource constraints.

### OTLP Export

OpenTelemetry Protocol (OTLP) is a standard protocol for exporting observability data.

This example exports traces to an OTLP-compatible collector.

The default endpoint is:

```text
http://localhost:4318/v1/traces
```

The endpoint can be overridden using:

```bash
OTEL_EXPORTER_OTLP_ENDPOINT
```

This allows the application to use different OpenTelemetry collectors across environments without changing application code.

---

## OpenTelemetry

OpenTelemetry is an open-source observability framework that provides APIs, SDKs, and tools for collecting and exporting telemetry data.

It supports:

- Distributed tracing.
- Metrics.
- Logs.
- Context propagation.
- Telemetry export.

In this example, OpenTelemetry is responsible for exporting trace data to Jaeger through OTLP.

Micrometer provides the application-facing tracing abstractions, while OpenTelemetry provides the tracing implementation and export integration.

---

## Jaeger

Jaeger is a distributed tracing platform used to collect, store, and visualize traces.

It provides a user interface for exploring traces and understanding how requests move through distributed applications.

This example uses Jaeger's all-in-one distribution for local development.

### Starting Jaeger

The example includes a `docker-compose.yml` configuration.

Start Jaeger from the distributed-tracing example directory:

```bash
docker compose up -d
```

The configuration exposes:

| Port    | Purpose                |
|---------|------------------------|
| `16686` | Jaeger user interface. |
| `4317`  | OTLP gRPC receiver.    |
| `4318`  | OTLP HTTP receiver.    |

Open the Jaeger user interface:

```text
http://localhost:16686
```

### Running the Services

Start the Inventory Service:

```bash
mvn -pl dt-inventory-service spring-boot:run
```

Start the Order Service:

```bash
mvn -pl dt-order-service spring-boot:run
```

The default service ports are:

| Service           | Port |
|-------------------|-----:|
| Order Service     | 8081 |
| Inventory Service | 8082 |

### Submitting an Order

Submit an order using `curl`:

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-1001",
    "productId": "SKU-1001",
    "quantity": 2
  }'
```

The Order Service calls the Inventory Service and returns the result.

### Inspecting Traces in Jaeger

1. Open `http://localhost:16686`.
2. Select `dt-order-service` from the Service dropdown.
3. Select the desired operation or leave it set to all operations.
4. Choose an appropriate lookback period, such as Last Hour.
5. Click **Find Traces**.
6. Select a trace to inspect its spans.

The trace view allows you to explore:

- The services involved in the request.
- Individual operations.
- Span durations.
- Parent-child relationships.
- Trace and span identifiers.
- Timing information across service boundaries.

The exact span names and hierarchy depend on the instrumentation.

### Understanding the Trace

A simplified representation of the expected request flow is:

```text
Order Service
  │
  ├── HTTP POST /api/orders
  │
  ├── order.process
  │
  └── HTTP Client Request
          │
          ▼
      Inventory Service
          │
          ├── HTTP GET /api/inventory/{productId}
          │
          └── inventory.lookup
```

This helps identify which operations contribute to the overall request duration.

---

## Testing Distributed Tracing

The example centralizes its tests in the `dt-integration-tests` module.

This module contains unit tests for service behavior and integration tests for HTTP-level behavior.

### Unit Testing Inventory Service

The Inventory Service tests verify:

- Returning the expected quantity for an existing product.
- Returning zero quantity for an unknown product.
- Retrieving the current trace ID when a span exists.
- Handling the absence of an active span.

### Unit Testing Order Service

The Order Service tests verify:

- Accepting an order when sufficient inventory exists.
- Rejecting an order when inventory is insufficient.
- Rejecting an order when the product is unavailable.
- Capturing the current trace ID.
- Handling failures from the Inventory Service.

### Integration Testing

The integration tests start both Spring Boot applications using randomly assigned ports.

They communicate through actual HTTP requests using the JDK `HttpClient`.

The tests verify:

| Test                   | Expected Result                    |
|------------------------|------------------------------------|
| Sufficient inventory   | HTTP 201, `ACCEPTED`               |
| Insufficient inventory | HTTP 201, `REJECTED`               |
| Unknown product        | HTTP 201, `REJECTED`               |
| Invalid order request  | HTTP 400                           |
| Trace propagation      | Matching trace IDs across services |

### Verifying Trace Propagation

The trace propagation test extracts the trace IDs from the response:

```java
String traceId = extractTraceId(TRACE_ID_PATTERN, response.body());
String inventoryTraceId = extractTraceId(INVENTORY_TRACE_ID_PATTERN, response.body());

assertThat(traceId).isEqualTo(inventoryTraceId);
```

This verifies that the Order Service and Inventory Service participated in the same distributed trace.

### Disabling Export During Tests

The integration tests disable OTLP exporting:

```text
--management.tracing.export.otlp.enabled=false
```

This prevents the tests from requiring a running OpenTelemetry collector.

Tracing itself remains enabled, allowing the tests to verify trace context propagation.

This distinction is important:

- Tracing creates and propagates trace information.
- Exporting sends collected trace data to an external observability backend.

The tests need the former but do not need the latter.

---

## Running the Example

From the repository root:

```bash
mvn -pl 09-spring-cloud/distributed-tracing/dt-order-service,09-spring-cloud/distributed-tracing/dt-inventory-service spring-boot:run
```

The services can also be started independently using their respective module commands.

Start Jaeger separately when you want to visualize exported traces.

---

## Running the Tests

Run the complete example and its tests from the repository root:

```bash
mvn -B clean verify \
  -pl 09-spring-cloud/distributed-tracing/dt-order-service,09-spring-cloud/distributed-tracing/dt-inventory-service,09-spring-cloud/distributed-tracing/dt-integration-tests \
  -am
```

This builds the required modules and executes their verification lifecycle.

The integration tests do not require Jaeger to be running because OTLP exporting is disabled during testing.

---

## Key Takeaways

- Distributed tracing makes it possible to follow requests across multiple services.
- A trace represents the complete journey of a request.
- A span represents an individual operation within a trace.
- Trace IDs identify complete traces, while span IDs identify individual operations.
- Trace context propagation connects operations across service boundaries.
- W3C Trace Context provides a standard mechanism for propagating tracing information.
- Spring Boot provides observability support through Micrometer.
- `ObservationRegistry` allows applications to instrument meaningful business operations.
- Micrometer Tracing provides access to the current trace and span.
- Spring Boot can automatically instrument supported HTTP clients and servers.
- The auto-configured `RestClient.Builder` should be used to retain Boot's HTTP client instrumentation.
- OpenTelemetry provides standardized telemetry collection and export capabilities.
- OTLP allows traces to be exported to compatible observability backends.
- Jaeger provides a user interface for inspecting distributed traces.
- Trace propagation can be tested independently of external trace exporting.
- Unit and integration tests verify both service behavior and distributed tracing behavior.

---

## Next Example

The next example will explore **Spring Cloud Declarative HTTP Clients**, focusing on declarative service-to-service communication, HTTP interface definitions, client-side load balancing, and integrating Spring HTTP Service Clients with Spring Cloud.
