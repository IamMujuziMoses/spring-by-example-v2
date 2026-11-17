# Resilience

This example demonstrates **resilience patterns in Spring Cloud using Resilience4j**.

It focuses on how a Spring application can handle failures in a downstream service using the circuit breaker pattern, how fallback responses keep an application responsive when dependencies become unavailable, and how a circuit breaker transitions between its different states to support recovery.

## What This Example Demonstrates

- What resilience means in distributed systems.
- Why downstream service failures affect dependent applications.
- The circuit breaker pattern.
- Spring Cloud CircuitBreaker.
- Resilience4j integration with Spring Cloud.
- Circuit breaker configuration using Java configuration.
- Circuit breaker states: `CLOSED`, `OPEN`, and `HALF_OPEN`.
- Failure rate thresholds and sliding windows.
- Minimum number of calls.
- Permitted calls in the half-open state.
- Fallback methods.
- Recovering when a downstream service becomes available.
- Integration testing with multiple Spring Boot application contexts.

---

## What Is Resilience?

Resilience is the ability of an application to continue operating, or degrade gracefully, when some of its dependencies fail.

In a distributed system, applications frequently communicate with other services over a network.

For example:

```text
Order Client
     |
     v
Order Service
     |
     v
Database
```

If the Order Service becomes unavailable, the Order Client may experience request failures.

Without appropriate resilience mechanisms, repeated calls to an unavailable service can cause unnecessary delays, resource consumption, and failures that affect other parts of the application.

Resilience patterns help applications handle these situations more gracefully.

This example focuses on the **circuit breaker pattern**.

---

## What Is the Circuit Breaker Pattern?

The circuit breaker pattern prevents an application from repeatedly calling a service that is currently failing.

It monitors calls to a dependency and tracks failures.

When the failure rate reaches a configured threshold, the circuit breaker opens and temporarily prevents further calls to the failing dependency.

Instead of making another downstream request, the application can execute a fallback operation.

The circuit breaker has three main states:

- `CLOSED`
- `OPEN`
- `HALF_OPEN`

Conceptually:

```text
             CLOSED
                |
                | Failure threshold reached
                v
              OPEN
                |
                | Wait duration expires
                v
            HALF_OPEN
             /     \
            /       \
     Successful    Failed
       trials      trial
          |           |
          v           v
       CLOSED       OPEN
```

Each state represents a different stage in the circuit breaker's handling of failures.

---

## Circuit Breaker States

### CLOSED

The circuit breaker starts in the `CLOSED` state.

Requests are allowed to reach the downstream service, and the circuit breaker monitors their outcomes.

Successful and failed calls are recorded within the configured sliding window.

When the failure rate reaches the configured threshold, the circuit breaker transitions to `OPEN`.

```text
Order Client
     |
     v
Circuit Breaker
   CLOSED
     |
     v
Order Service
```

### OPEN

When the configured failure threshold is reached, the circuit breaker transitions to `OPEN`.

Requests are no longer forwarded to the downstream service.

Instead, the circuit breaker rejects calls and allows the application to execute its fallback operation.

```text
Order Client
     |
     v
Circuit Breaker
     OPEN
     |
     X
     |
   Fallback
     |
     v
Fallback Response
```

The circuit remains open for the configured wait duration.

This prevents the application from continuously sending requests to a dependency that is likely still unavailable.

### HALF_OPEN

After the open-state wait duration expires, the circuit breaker transitions to `HALF_OPEN` when another request arrives.

In this state, a limited number of trial requests are permitted to reach the downstream service.

These requests help determine whether the service has recovered.

If the permitted trial calls succeed, the circuit breaker transitions to `CLOSED`.

If the failure threshold is reached during the trial period, the circuit breaker transitions back to `OPEN`.

```text
             HALF_OPEN
                 |
          Trial requests
                 |
          +------+------+
          |             |
      Successful      Failed
        trials         trial
          |             |
          v             v
       CLOSED         OPEN
```

---

## Spring Cloud CircuitBreaker

Spring Cloud CircuitBreaker provides an abstraction for using circuit breaker implementations in Spring applications.

It allows applications to work with a consistent circuit breaker API without directly coupling application code to a particular implementation.

This example uses **Resilience4j** as the underlying circuit breaker implementation.

The dependency used by the client application is:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

Spring Cloud CircuitBreaker provides the integration between the application and Resilience4j.

The application uses `CircuitBreakerFactory` to create a circuit breaker.

---

## Example Architecture

This example contains three Maven modules:

### Order Service

The Order Service represents a downstream dependency.

It exposes an endpoint for retrieving an order:

```http
GET /orders/{orderId}
```

Under normal conditions, it returns an order response.

It also provides a mechanism for simulating failures so that the circuit breaker behavior can be demonstrated and tested.

### Order Client

The Order Client calls the Order Service through Spring's `RestClient`.

It uses a circuit breaker to protect the downstream call.

If the downstream request fails or the circuit is open, the client executes a fallback operation.

The client exposes:

```http
GET /client/orders/{orderId}
```

### Integration Tests

The integration tests start both applications in separate Spring application contexts.

They communicate over HTTP using randomly assigned ports.

This allows the tests to exercise actual application-to-application communication rather than mocking the downstream service.

---

## Circuit Breaker Configuration

This example configures the circuit breaker using a Spring Cloud CircuitBreaker `Customizer`.

The configuration is defined in `ResilienceConfiguration`:

```java
@Configuration
public class ResilienceConfiguration {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> orderServiceCircuitBreakerCustomizer() {
        return factory -> factory.configure(
                builder -> builder.circuitBreakerConfig(
                        CircuitBreakerConfig.custom()
                                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                                .slidingWindowSize(4)
                                .minimumNumberOfCalls(4)
                                .failureRateThreshold(50)
                                .waitDurationInOpenState(Duration.ofMillis(200))
                                .permittedNumberOfCallsInHalfOpenState(2)
                                .automaticTransitionFromOpenToHalfOpenEnabled(false)
                                .build()
                ), "orderService");
    }
}
```

The customizer configures the circuit breaker identified by `orderService`.

### Configuration Properties

| Property                                       |         Value | Description                                                                    |
|------------------------------------------------|--------------:|--------------------------------------------------------------------------------|
| `slidingWindowType`                            | `COUNT_BASED` | Evaluates failures using a count-based sliding window.                         |
| `slidingWindowSize`                            |           `4` | Maintains statistics for the most recent four calls.                           |
| `minimumNumberOfCalls`                         |           `4` | Requires at least four recorded calls before calculating the failure rate.     |
| `failureRateThreshold`                         |          `50` | Opens the circuit when the failure rate reaches 50%.                           |
| `waitDurationInOpenState`                      |       `200ms` | Determines how long the circuit remains open before recovery can be attempted. |
| `permittedNumberOfCallsInHalfOpenState`        |           `2` | Allows two trial calls while the circuit is half-open.                         |
| `automaticTransitionFromOpenToHalfOpenEnabled` |       `false` | Transitions to half-open when a call arrives after the wait duration expires.  |

### Understanding the Sliding Window

The sliding window determines how many recent calls are considered when calculating the failure rate.

This example uses:

```java
.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
.slidingWindowSize(4)
.minimumNumberOfCalls(4)
```

The circuit breaker evaluates the most recent four calls.

The minimum number of calls ensures that the failure rate is not calculated until four calls have been recorded.

For example:

| Total Calls | Failed Calls |  Failure Rate | Circuit State |
|------------:|-------------:|--------------:|---------------|
|           1 |            1 | Not evaluated | CLOSED        |
|           2 |            2 | Not evaluated | CLOSED        |
|           3 |            3 | Not evaluated | CLOSED        |
|           4 |            4 |          100% | OPEN          |

Since the failure rate reaches 100%, which exceeds the configured 50% threshold, the circuit breaker opens.

---

## Creating the Circuit Breaker

The `OrderServiceClient` obtains a circuit breaker through Spring Cloud CircuitBreaker's `CircuitBreakerFactory`.

```java
@Component
public class OrderServiceClient {

    private static final String CIRCUIT_BREAKER_ID = "orderService";

    private final RestClient restClient;

    private final CircuitBreaker circuitBreaker;

    public OrderServiceClient(RestClient.Builder restClientBuilder, CircuitBreakerFactory<?, ?> circuitBreakerFactory,
            @Value("${app.order-service.base-url}") String orderServiceBaseUrl) {

        this.restClient = restClientBuilder.baseUrl(orderServiceBaseUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create(CIRCUIT_BREAKER_ID);
    }
}
```

The circuit breaker ID is:

```java
private static final String CIRCUIT_BREAKER_ID = "orderService";
```

This identifier connects the circuit breaker instance to the configuration defined in `ResilienceConfiguration`.

The factory creates a circuit breaker using the configuration associated with that ID.

---

## Protecting a Downstream Call

The `getOrder()` method executes the downstream HTTP request through the circuit breaker.

```java
public OrderResponse getOrder(String orderId) {
    return circuitBreaker.run(
            () -> restClient.get().uri("/orders/{orderId}", orderId).retrieve().body(OrderResponse.class),
            throwable -> fallback(orderId));
}
```

The `run()` method accepts two operations:

1. The primary operation.
2. A fallback operation that receives the exception when the primary operation fails or the circuit breaker rejects the call.

The primary operation calls the Order Service.

If the request succeeds, its response is returned.

If the request fails, the fallback operation is executed.

---

## Implementing a Fallback

The fallback method provides an alternative response when the downstream service is unavailable.

```java
private OrderResponse fallback(String orderId) {
    return new OrderResponse(orderId, "UNAVAILABLE", null, true, "Order service is temporarily unavailable");
}
```

The fallback response indicates that the requested order could not be retrieved from the downstream service.

The `fallback` field identifies that the response was produced by the fallback operation.

For example:

```json
{
  "orderId": "1001",
  "status": "UNAVAILABLE",
  "instanceId": null,
  "fallback": true,
  "message": "Order service is temporarily unavailable"
}
```

A fallback does not necessarily mean that the original operation succeeded.

It provides a controlled response when the primary operation cannot be completed.

In a production application, fallback behavior should reflect the business requirements. It might return cached data, a default response, or a meaningful service-unavailable result.

---

## Simulating Downstream Failures

The Order Service includes a failure simulation mechanism.

This allows the example to demonstrate how the circuit breaker responds when the downstream service starts failing.

The `FailureModeService` maintains the failure state and request count.

Conceptually:

```text
FailureModeService
       |
       +-- Failure Enabled
       |
       +-- Request Counter
```

When failure simulation is enabled, the Order Service responds with HTTP `503 Service Unavailable`.

Otherwise, it returns a successful order response.

This makes it possible to test both normal and failure scenarios without stopping or restarting the application.

The failure simulation is intended for this educational example, not as a production failure-management mechanism.

---

## Circuit Breaker Request Flow

The normal request flow looks like:

```text
HTTP Request
     |
     v
Order Client Controller
     |
     v
OrderServiceClient
     |
     v
Circuit Breaker
   CLOSED
     |
     v
RestClient
     |
     v
Order Service
     |
     v
Successful Response
```

When the downstream service starts failing:

```text
HTTP Request
     |
     v
Order Client Controller
     |
     v
OrderServiceClient
     |
     v
Circuit Breaker
   CLOSED
     |
     v
RestClient
     |
     v
Order Service
     |
     v
503 Service Unavailable
     |
     v
Failure Recorded
```

After the failure threshold is reached:

```text
HTTP Request
     |
     v
Order Client Controller
     |
     v
OrderServiceClient
     |
     v
Circuit Breaker
    OPEN
     |
     X
     |
   Fallback
     |
     v
Fallback Response
```

When the downstream service recovers:

```text
Circuit Breaker
      OPEN
        |
        | Wait duration expires
        v
    HALF_OPEN
        |
        | Trial requests
        v
   Order Service
        |
        | Successful responses
        v
     CLOSED
```

---

## Testing Resilience

The example uses Spring Boot test support and JUnit to verify the circuit breaker behavior through integration tests.

The integration tests start two independent Spring Boot applications:

- Order Service.
- Order Client.

Each application runs in its own Spring application context and uses a randomly assigned HTTP port.

The test communicates with the Order Client through `RestClient`, while the Order Client communicates with the actual Order Service.

This demonstrates resilience behavior across an application boundary.

### Starting the Applications

The test starts the Order Service:

```java
orderServiceApplication = new SpringApplicationBuilder(OrderServiceApplication.class)
        .run(
                "--server.port=0",
                "--app.instance-id=order-service",
                "--spring.main.banner-mode=off"
        );
```

It then retrieves the assigned port:

```java
Integer orderServicePort = orderServiceApplication.getEnvironment().getRequiredProperty("local.server.port", Integer.class);
```

The Order Client is started with the Order Service URL:

```java
orderClientApplication = new SpringApplicationBuilder(OrderClientApplication.class)
        .run(
                "--server.port=0",
                "--app.order-service.base-url=http://localhost:" + orderServicePort,
                "--spring.main.banner-mode=off"
        );
```

Using `server.port=0` allows the operating system to assign an available port.

This avoids hardcoded port conflicts during testing.

### Successful Request

The first test verifies that a request succeeds when the Order Service is available.

```java
@Test
void shouldReturnSuccessfulOrderWhenServiceIsAvailable() {
    OrderResponse response = requestOrder(ORDER_ID);

    assertThat(response.orderId()).isEqualTo(ORDER_ID);
    assertThat(response.status()).isEqualTo("PROCESSED");
    assertThat(response.instanceId()).isEqualTo("order-service");
    assertThat(response.fallback()).isFalse();
    assertThat(failureModeService.getRequestCount()).isEqualTo(1);
}
```

The test verifies that:

- The requested order ID is returned.
- The order status is `PROCESSED`.
- The response identifies the Order Service instance.
- No fallback was used.
- The downstream service received exactly one request.

### Fallback When the Service Fails

The second test enables failure simulation.

```java
@Test
void shouldReturnFallbackWhenOrderServiceFails() {
    failureModeService.setFailureEnabled(true);

    OrderResponse response = requestOrder(ORDER_ID);

    assertThat(response.orderId()).isEqualTo(ORDER_ID);
    assertThat(response.status()).isEqualTo("UNAVAILABLE");
    assertThat(response.fallback()).isTrue();
    assertThat(response.message()).isEqualTo("Order service is temporarily unavailable");
    assertThat(failureModeService.getRequestCount()).isEqualTo(1);
}
```

The test verifies that:

- The downstream service receives the request.
- The downstream request fails.
- The circuit breaker invokes the fallback operation.
- The client returns an unavailable response.

### Opening the Circuit Breaker

The third test verifies that the circuit breaker opens after the configured failure threshold is reached.

```java
@Test
void shouldOpenCircuitAfterFailureThresholdIsReached() {
    failureModeService.setFailureEnabled(true);

    makeFailedRequests(4);

    assertThat(failureModeService.getRequestCount()).isEqualTo(4);

    OrderResponse fallbackResponse = requestOrder(ORDER_ID);

    assertThat(fallbackResponse.fallback()).isTrue();
    assertThat(failureModeService.getRequestCount()).isEqualTo(4);
}
```

The first four requests reach the Order Service and fail.

Since the configured minimum number of calls is four and the failure rate is 100%, the circuit breaker transitions to `OPEN`.

The fifth request is rejected by the circuit breaker.

The fallback is returned without another request reaching the Order Service.

The request count therefore remains four.

### Recovering After the Service Becomes Available

The fourth test demonstrates recovery.

```java
@Test
void shouldRecoverAfterOrderServiceBecomesAvailable() throws InterruptedException {
    failureModeService.setFailureEnabled(true);

    makeFailedRequests(4);

    assertThat(failureModeService.getRequestCount()).isEqualTo(4);

    failureModeService.setFailureEnabled(false);

    Thread.sleep(300);

    IntStream.range(0, 3)
            .forEach(index -> {
                OrderResponse response = requestOrder(ORDER_ID);
                assertThat(response.fallback()).isFalse();
            });

    assertThat(failureModeService.getRequestCount()).isEqualTo(7);
}
```

The test follows these steps:

1. Enables failure simulation.
2. Sends four requests that fail.
3. Verifies that the downstream service received four requests.
4. Disables failure simulation.
5. Waits for the configured open-state duration to expire.
6. Sends successful trial requests.
7. Verifies that the application can successfully communicate with the recovered service.

The circuit breaker allows trial requests in the `HALF_OPEN` state.

Successful trial requests allow it to transition back to `CLOSED`.

The example configures two permitted calls in the half-open state, while the test sends a third request to verify that normal operation continues after recovery.

---

## Resetting the Circuit Breaker Between Tests

The integration tests reuse the same Spring application contexts.

This means that the circuit breaker registry and its circuit breaker instances can persist between test methods.

The test therefore resets the failure simulation and any existing circuit breaker before each test.

```java
@BeforeEach
void resetApplications() {
    failureModeService.reset();
    circuitBreakerRegistry.find("orderService").ifPresent(CircuitBreaker::reset);
}
```

An important detail is the use of:

```java
circuitBreakerRegistry.find("orderService")
```

rather than:

```java
circuitBreakerRegistry.circuitBreaker("orderService")
```

The `circuitBreaker()` method can create a new circuit breaker if one does not already exist.

Creating it directly from the registry could initialize it with the registry's default configuration before the Spring Cloud CircuitBreaker factory creates it with its intended configuration.

Using `find()` avoids creating a new instance merely to reset an existing one.

This preserves the circuit breaker's configured settings between test executions.

---

## Why Use Integration Tests?

Circuit breaker behavior depends on the interaction between several components:

- Spring Cloud CircuitBreaker.
- Resilience4j.
- The Order Client.
- The downstream Order Service.
- HTTP request execution.
- Fallback handling.

Testing only individual classes would not demonstrate the complete request flow.

This example therefore starts both applications and tests their interaction over HTTP.

The integration tests verify that the circuit breaker protects the client from repeated calls to an unavailable downstream service.

---

## Running the Example

From the repository root, build the example:

```bash
mvn -pl 09-spring-cloud/resilience -am clean install
```

The example consists of separate applications, so start them independently.

Start the Order Service:

```bash
mvn -pl 09-spring-cloud/resilience/resilience-order-service spring-boot:run
```

Start the Order Client in another terminal:

```bash
mvn -pl 09-spring-cloud/resilience/resilience-order-client spring-boot:run
```

The default configuration uses:

| Application   | Port |
|---------------|-----:|
| Order Client  | 8080 |
| Order Service | 8081 |

### Retrieve an Order

```bash
curl http://localhost:8080/client/orders/1001
```

The request should return a successful order response.

---

## Running the Tests

Run the integration tests from the example directory:

```bash
mvn -pl 09-spring-cloud/resilience/resilience-integration-tests -am test
```

Or run them from the integration test module:

```bash
cd 09-spring-cloud/resilience/resilience-integration-tests
mvn test
```

The tests verify:

- Successful downstream communication.
- Fallback execution when the downstream service fails.
- Circuit breaker activation after the failure threshold is reached.
- Recovery after the downstream service becomes available.

---

## Key Takeaways

- Resilience helps applications handle failures in distributed systems.
- The circuit breaker pattern prevents repeated calls to failing dependencies.
- Spring Cloud CircuitBreaker provides an abstraction for integrating circuit breaker implementations.
- Resilience4j provides the circuit breaker implementation used in this example.
- A circuit breaker has three states: `CLOSED`, `OPEN`, and `HALF_OPEN`.
- The sliding window determines which recent calls are considered when calculating the failure rate.
- The minimum number of calls determines when failure-rate evaluation can begin.
- The failure rate threshold determines when the circuit breaker opens.
- Fallback operations provide alternative responses when downstream calls fail or are rejected.
- The open-state wait duration controls when recovery can be attempted.
- The half-open state allows limited trial requests to determine whether a dependency has recovered.
- Integration tests can verify resilience behavior across independently running Spring Boot applications.
- Circuit breaker state and configuration should be handled carefully when sharing application contexts between tests.

---

## Next Example

The next example will explore **Distributed Tracing** in Spring Cloud, demonstrating how to trace requests across multiple services, propagate trace context, and observe request flows in distributed applications.
