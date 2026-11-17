# Spring Cloud Load Balancing

This example demonstrates **client-side load balancing** using Spring Cloud LoadBalancer and Spring Cloud's `SimpleDiscoveryClient`.

It focuses on how a client application can communicate with multiple instances of the same service using a logical service name, how Spring Cloud LoadBalancer selects a service instance, and how requests can be distributed across available instances.

The example uses Spring's `RestClient` with the `@LoadBalanced` annotation and the default round-robin load-balancing strategy.

## What This Example Demonstrates

- What client-side load balancing means.
- Why applications run multiple instances of the same service.
- The relationship between service discovery and load balancing.
- Spring Cloud LoadBalancer.
- `SimpleDiscoveryClient`.
- Logical service names and service instance resolution.
- The `@LoadBalanced` annotation.
- Using `RestClient` with Spring Cloud LoadBalancer.
- The default round-robin load-balancing strategy.
- Configuring multiple service instances through application properties.
- Distributing requests across multiple service instances.
- Handling situations where no service instances are available.
- Testing load-balancing behavior through integration tests.

---

## What Is Load Balancing?

Load balancing is the process of distributing incoming requests across multiple instances of a service.

Instead of sending every request to a single application instance, requests are distributed across available instances.

This helps applications handle increased traffic and improves resource utilization.

Consider an order service with two running instances:

```text
                   Client
                     |
                     |
                     v
                Load Balancer
                     |
                /          \
               /            \
              v              v
       Order Service 1  Order Service 2
           :8081            :8082
```

The load balancer selects an instance to handle each request.

For example:

```text
Request 1  ──────────> Order Service 1
Request 2  ──────────> Order Service 2
Request 3  ──────────> Order Service 1
Request 4  ──────────> Order Service 2
```

This allows multiple instances to share the incoming workload.

---

## What Is Client-Side Load Balancing?

Client-side load balancing means that the application making an HTTP request is responsible for selecting the destination service instance.

The client does not need to know which physical instance will handle a request.

Instead, it communicates with a logical service name.

For example:

```text
http://lb-order-service/orders/1001
```

The logical name `lb-order-service` represents the order service rather than a specific host and port.

Spring Cloud LoadBalancer resolves that logical name into an available service instance and selects the destination.

Conceptually:

```text
Order Client
     |
     | HTTP Request
     |
     v
http://lb-order-service/orders/1001
     |
     v
Spring Cloud LoadBalancer
     |
     v
Service Instance Selection
     |
     +------------------+
     |                  |
     v                  v
Order Service 1    Order Service 2
```

The client application does not need to construct URLs containing the physical address of each service instance.

---

## Spring Cloud LoadBalancer

Spring Cloud LoadBalancer is a client-side load-balancing solution in the Spring Cloud ecosystem.

It provides mechanisms for selecting service instances when an application communicates with another service through a logical service name.

It integrates with Spring HTTP clients, including:

- `RestClient`
- `RestTemplate`
- `WebClient`

This example uses `RestClient`.

The load balancer works with service discovery to obtain available service instances.

Its responsibility is to select an instance for an outgoing request.

---

## Service Discovery

Service discovery provides information about the available instances of a service.

For example, the order service might have two running instances:

| Service          | Host      | Port |
|------------------|-----------|------|
| lb-order-service | localhost | 8081 |
| lb-order-service | localhost | 8082 |

The service identifier is the same, but each instance has a different address.

A load balancer needs this information to determine where requests can be sent.

Conceptually:

```text
Service Discovery
       |
       v
Available Instances
       |
       +-------------------+
       |                   |
       v                   v
  Instance 1           Instance 2
  localhost:8081       localhost:8082
       |                   |
       +---------+---------+
                 |
                 v
          Load Balancer
```

This example uses `SimpleDiscoveryClient` to provide service instance information.

---

## SimpleDiscoveryClient

`SimpleDiscoveryClient` is a Spring Cloud implementation of `DiscoveryClient` that obtains service instances from application configuration properties.

It is useful when service instances are known in advance and do not need to register dynamically with a service registry.

The example configures two instances of `lb-order-service`:

```yaml
spring:
  cloud:
    discovery:
      client:
        simple:
          instances:
            lb-order-service:
              - uri: http://localhost:8081
              - uri: http://localhost:8082
```

This configuration tells Spring Cloud that the logical service name `lb-order-service` has two available instances.

When the client makes a request to that service name, Spring Cloud LoadBalancer can select either instance.

### Important Distinction

`SimpleDiscoveryClient` does not provide dynamic service registration or automatic instance registration.

The service instances are configured explicitly.

This keeps the example focused on client-side load balancing without introducing an external service registry such as Eureka.

---

## The @LoadBalanced Annotation

Spring Cloud provides the `@LoadBalanced` annotation to mark HTTP client builders that should support load-balanced requests.

The example defines a `RestClient.Builder` bean:

```java
@Configuration(proxyBeanMethods = false)
public class LoadBalancerConfiguration {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }
}
```

The `@LoadBalanced` annotation allows Spring Cloud LoadBalancer to participate in requests made through this client.

Without the load-balancing integration, an HTTP client would normally interpret a hostname as a regular network destination.

With the integration, the logical service name can be resolved to an available service instance.

---

## Using RestClient with Spring Cloud LoadBalancer

The example uses Spring's `RestClient` to communicate with the order service.

The client is configured with the logical service name:

```java
@Component
public class OrderServiceClient {

    private final RestClient restClient;

    public OrderServiceClient(@LoadBalanced RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("http://lb-order-service").build();
    }

    public OrderResponse getOrder(String orderId) {
        return restClient.get().uri("/orders/{orderId}", orderId).retrieve().body(OrderResponse.class);
    }
}
```

Notice that the client does not use:

```java
.baseUrl("http://localhost:8081")
```

Instead, it uses:

```java
.baseUrl("http://lb-order-service")
```

The logical service name is resolved by Spring Cloud LoadBalancer when the request is executed.

This separates the client from the physical location of the service instances.

---

## Round-Robin Load Balancing

Spring Cloud LoadBalancer uses a round-robin implementation by default.

Round-robin distributes requests by selecting service instances sequentially.

Suppose two order service instances are available:

```text
Order Service 1
Order Service 2
```

Requests are distributed as follows:

```text
Request 1  ──────────> Order Service 1
Request 2  ──────────> Order Service 2
Request 3  ──────────> Order Service 1
Request 4  ──────────> Order Service 2
Request 5  ──────────> Order Service 1
Request 6  ──────────> Order Service 2
```

The selection continues cycling through the available instances.

For two healthy instances receiving ten sequential requests, the expected distribution is:

| Service Instance | Requests |
|------------------|---------:|
| Order Service 1  |        5 |
| Order Service 2  |        5 |
| Total            |       10 |

The exact sequence depends on the starting selection, but the default round-robin strategy distributes sequential requests evenly when the same two instances remain available.

---

## Order Service

The example contains an order service that exposes an endpoint for retrieving an order.

The endpoint is:

```http
GET /orders/{orderId}
```

For example:

```bash
curl http://localhost:8081/orders/1001
```

A response looks like:

```json
{
  "orderId": "1001",
  "instanceId": "order-service-instance-1"
}
```

The response contains:

- `orderId`: The requested order identifier.
- `instanceId`: The identifier of the service instance handling the request.

The instance identifier makes it possible to observe which service instance receives a request.

Each running instance can be configured with a different identifier:

```text
order-service-instance-1
order-service-instance-2
```

This makes request distribution visible during testing.

---

## Order Client

The order client exposes an endpoint that delegates requests to the order service through the load-balanced HTTP client.

Its endpoint is:

```http
GET /client/orders/{orderId}
```

For example:

```bash
curl http://localhost:8080/client/orders/1001
```

The request flow is:

```text
HTTP Request
     |
     v
OrderClientController
     |
     v
OrderServiceClient
     |
     v
RestClient
     |
     v
Spring Cloud LoadBalancer
     |
     v
Selected Order Service Instance
     |
     v
Order Response
```

The order client does not select a physical service instance itself.

It delegates instance selection to Spring Cloud LoadBalancer.

---

## Handling Multiple Service Instances

The example runs two independent instances of the order service.

Each instance:

- Runs as a separate Spring Boot application context.
- Uses a different server port.
- Has a unique instance identifier.
- Exposes the same order endpoint.

The client discovers both instances through `SimpleDiscoveryClient`.

The load balancer can then distribute requests between them.

Conceptually:

```text
                  Order Client
                       |
                       |
                       v
             Spring Cloud LoadBalancer
                       |
                 Round Robin
                  /       \
                 /         \
                v           v
       Order Service 1  Order Service 2
          :8081            :8082
                \           /
                 \         /
                  v       v
                Order Response
```

---

## What Happens When No Instances Are Available?

A load balancer needs at least one available service instance to execute a request.

If service discovery returns no instances for the requested service, Spring Cloud LoadBalancer cannot select a destination.

In this situation, the load-balanced request fails.

For the blocking `RestClient` integration used in this example, an `IllegalStateException` is raised when no instances are available.

Conceptually:

```text
Order Client
     |
     v
Spring Cloud LoadBalancer
     |
     v
Service Discovery
     |
     v
No Available Instances
     |
     v
IllegalStateException
```

The integration tests include a scenario that verifies this behavior.

This example demonstrates the no-instance failure case. It does not implement a custom fallback or retry mechanism.

---

## SimpleDiscoveryClient vs Service Registry

There are different ways to obtain service instance information.

This example uses `SimpleDiscoveryClient`, which reads instance information from configuration.

Another approach is to use a service registry such as Eureka.

### SimpleDiscoveryClient

```text
Application Configuration
          |
          v
  SimpleDiscoveryClient
          |
          v
  Available Instances
          |
          v
  Spring Cloud LoadBalancer
```

Service instances are configured explicitly.

### Service Registry

```text
Order Service Instances
          |
          v
    Service Registry
          |
          v
    Service Discovery
          |
          v
  Spring Cloud LoadBalancer
```

Service instances can register with and be discovered through a registry.

The choice depends on the application's infrastructure and service discovery requirements.

This example intentionally uses `SimpleDiscoveryClient` to isolate the load-balancing concepts.

---

## Client-Side vs Server-Side Load Balancing

Load balancing can be implemented in different places.

### Client-Side Load Balancing

The client application selects the destination service instance.

```text
Client
  |
  v
Client-Side Load Balancer
  |
  +----------+
  |          |
  v          v
Service 1  Service 2
```

Spring Cloud LoadBalancer is an example of client-side load balancing.

### Server-Side Load Balancing

A separate load balancer receives requests and forwards them to the appropriate service instance.

```text
Client
  |
  v
External Load Balancer
  |
  +----------+
  |          |
  v          v
Service 1  Service 2
```

In server-side load balancing, the client generally communicates with the load balancer rather than selecting an individual service instance.

---

## Testing Load Balancing

The example uses Spring Boot's test support and JUnit to verify load-balancing behavior through integration tests.

The tests start two order service instances and an order client application.

The order client is configured with the addresses of the two service instances.

All requests are made through the order client, ensuring that the tests exercise the actual HTTP client and Spring Cloud LoadBalancer integration.

### 1. Returning an Order from an Available Service Instance

The test verifies that the client can retrieve an order from an available service instance.

It checks that:

- A response is returned.
- The requested order ID is correct.
- The response identifies one of the configured instances.

### 2. Distributing Requests Across Service Instances

The test sends consecutive requests through the order client.

It verifies that both order service instances receive requests.

This demonstrates that the load balancer does not continually direct requests to a single instance.

### 3. Even Round-Robin Distribution

The test sends ten sequential requests.

It counts the responses from each service instance and verifies the distribution:

```text
Order Service 1: 5 requests
Order Service 2: 5 requests
```

This demonstrates the default round-robin behavior with two available instances.

### 4. Using Configured Service Instances

The test obtains the available instances from `DiscoveryClient`.

It verifies that the discovered instances match the two addresses configured for `lb-order-service`.

This checks the service discovery configuration independently of the HTTP request assertions.

### 5. Handling Unavailable Service Instances

The test starts a separate order client application without configuring any order service instances.

It then attempts to retrieve an order.

The test verifies that an `IllegalStateException` is raised because no service instances are available.

### Integration Test Coverage

| Test                                                    | Behavior Verified                                   |
|---------------------------------------------------------|-----------------------------------------------------|
| `shouldReturnOrderFromAvailableServiceInstance`         | Retrieves an order from an available instance.      |
| `shouldDistributeRequestsAcrossOrderServiceInstances`   | Distributes requests across both instances.         |
| `shouldDistributeRequestsEvenlyUsingRoundRobinStrategy` | Verifies five requests per instance out of ten.     |
| `shouldUseConfiguredServiceInstances`                   | Verifies the discovered service instance addresses. |
| `shouldHandleUnavailableServiceInstances`               | Verifies behavior when no instances are available.  |

---

## Running the Example

First, build and install the example modules from the repository root:

```bash
mvn -pl 09-spring-cloud/load-balancing -am install
```

Start the first order service instance:

```bash
cd 09-spring-cloud/load-balancing/lb-order-service

mvn spring-boot:run \
    -Dspring-boot.run.arguments="--server.port=8081 --app.instance-id=order-service-instance-1"
```

Start the second order service instance in another terminal:

```bash
cd 09-spring-cloud/load-balancing/lb-order-service

mvn spring-boot:run \
    -Dspring-boot.run.arguments="--server.port=8082 --app.instance-id=order-service-instance-2"
```

Start the order client in another terminal:

```bash
cd 09-spring-cloud/load-balancing/lb-order-client

mvn spring-boot:run
```

The applications use the following default ports:

| Application              | Port |
|--------------------------|-----:|
| Order Client             | 8080 |
| Order Service Instance 1 | 8081 |
| Order Service Instance 2 | 8082 |

---

## Sending Requests

Send requests through the order client:

```bash
curl http://localhost:8080/client/orders/1001
```

Repeated requests should return responses identifying different order service instances.

Example responses:

```json
{
  "orderId": "1001",
  "instanceId": "order-service-instance-1"
}
```

```json
{
  "orderId": "1001",
  "instanceId": "order-service-instance-2"
}
```

The instance identifier allows you to observe how requests are distributed.

---

## Running the Tests

Run the integration tests from the example:

```bash
mvn -pl 09-spring-cloud/load-balancing -am verify
```

The tests start all required applications automatically using random ports.

No manually running service instances are required when executing the integration tests.

---

## Key Takeaways

- Load balancing distributes requests across multiple service instances.
- Client-side load balancing allows the client to select a destination instance.
- Spring Cloud LoadBalancer provides client-side load-balancing capabilities.
- Service discovery supplies information about available service instances.
- `SimpleDiscoveryClient` provides service instances through application configuration.
- `@LoadBalanced` enables service-name resolution for supported HTTP clients.
- `RestClient` can communicate with services through logical service names.
- Spring Cloud LoadBalancer uses round-robin selection by default.
- Multiple service instances can share incoming requests.
- A request cannot be routed when no service instances are available.
- Service discovery and load balancing are related but distinct responsibilities.
- Integration tests can verify instance discovery, request distribution, and failure behavior.

---

## Next

The next example will explore **Resilience** in Spring Cloud, focusing on handling service failures, implementing fault-tolerance patterns such as retries and circuit breakers, and building more resilient distributed applications.
