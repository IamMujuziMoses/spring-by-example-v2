# API Gateway

This example demonstrates **API Gateway** using Spring Cloud Gateway.

It focuses on how an API Gateway provides a single entry point to multiple backend services, how incoming requests are matched to routes, and how gateway filters can modify requests and responses.

The example uses Spring Cloud Gateway WebFlux as the gateway server and Spring MVC for the backend services.

## What This Example Demonstrates

- What an API Gateway is and why it is useful in microservice architectures.
- The role of an API Gateway in a distributed system.
- Spring Cloud Gateway and its WebFlux implementation.
- Gateway routes and route identifiers.
- Path-based route predicates.
- Gateway filters.
- The `StripPrefix` filter.
- The `AddResponseHeader` filter.
- Routing requests to multiple backend services.
- Configuring backend service URLs externally.
- Running the gateway independently from backend services.
- Testing gateway routing using integration tests.
- Handling unmatched routes with HTTP 404 responses.

---

## What Is an API Gateway?

An API Gateway is a server that acts as a single entry point for clients accessing multiple backend services.

Instead of requiring clients to communicate directly with individual services, requests are sent through the gateway.

The gateway determines which backend service should receive each request and forwards the request accordingly.

A simplified architecture looks like:

```text
                  Client
                    │
                    ▼
              API Gateway
                    │
          ┌─────────┴─────────┐
          │                   │
          ▼                   ▼
     Order Service       Inventory Service
          │                   │
          ▼                   ▼
     Order Data          Inventory Data
```

For example, a client can access different services through the same gateway:

```text
GET /api/orders/ORD-1001
GET /api/inventory/SKU-1001
```

The gateway routes each request to the appropriate backend service.

Clients therefore do not need to know the internal addresses of individual services.

---

## Why Use an API Gateway?

In a microservice architecture, applications are divided into independently deployable services.

Without an API Gateway, clients may need to communicate directly with multiple backend services.

For example:

```text
Client
  │
  ├──────────────► Order Service
  │
  ├──────────────► Inventory Service
  │
  └──────────────► Customer Service
```

This introduces several challenges:

- Clients need to know the addresses of individual services.
- Changes to service locations may affect clients.
- Authentication and other cross-cutting concerns may need to be implemented repeatedly.
- Clients must manage communication with multiple services.
- Exposing internal services directly increases architectural coupling.

An API Gateway provides a centralized entry point:

```text
Client
  │
  ▼
API Gateway
  │
  ├──────────────► Order Service
  │
  ├──────────────► Inventory Service
  │
  └──────────────► Customer Service
```

This allows the gateway to centralize responsibilities such as:

- Request routing.
- Authentication and authorization.
- Request and response transformation.
- Rate limiting.
- Logging and monitoring.
- Cross-origin resource sharing (CORS).
- Resilience and request handling.

Not every gateway needs to implement all these responsibilities. Its configuration depends on the application's requirements.

---

## Spring Cloud Gateway

[Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/reference/) provides an API Gateway implementation built on Spring.

It provides routing and filtering capabilities that allow applications to define how incoming requests are handled.

The main concepts include:

### Routes

A route defines where a request should be forwarded.

A route consists of:

- An identifier.
- A destination URI.
- Predicates that determine whether the route matches.
- Filters that modify the request or response.

### Predicates

Predicates determine whether an incoming request matches a route.

Examples include:

- `Path`: Matches requests based on their URL path.
- `Method`: Matches requests based on their HTTP method.
- `Host`: Matches requests based on their host.
- `Header`: Matches requests based on HTTP headers.
- `Query`: Matches requests based on query parameters.

### Filters

Filters modify incoming requests or outgoing responses.

They can be used to:

- Modify request headers.
- Modify response headers.
- Rewrite request paths.
- Remove path segments.
- Add authentication-related information.
- Implement other gateway-specific processing.

Conceptually:

```text
Incoming Request
       │
       ▼
  Route Matching
       │
       ▼
 Gateway Filters
       │
       ▼
Backend Service
       │
       ▼
 Gateway Filters
       │
       ▼
Outgoing Response
```

---

## Spring Cloud Gateway WebFlux

This example uses the Spring Cloud Gateway WebFlux implementation.

The gateway is built on Spring WebFlux and uses a reactive, non-blocking request-processing model.

Its dependency is:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
</dependency>
```

Spring Cloud Gateway WebFlux is designed to run on a reactive web stack.

It is incompatible with Spring MVC being present on the gateway application's classpath.

This distinction is important when working with multiple Spring applications in the same Maven project.

The gateway and the backend services in this example use different web application stacks:

| Application       | Web Stack      |
|-------------------|----------------|
| Gateway Server    | Spring WebFlux |
| Order Service     | Spring MVC     |
| Inventory Service | Spring MVC     |

The services can communicate over HTTP even though they use different Spring web stacks.

---

## Example Architecture

The example consists of four Maven modules:

```text
api-gateway/
│
├── ag-gateway-server/
│
├── ag-order-service/
│
├── ag-inventory-service/
│
└── ag-integration-tests/
```

### Gateway Server

The `ag-gateway-server` module contains the Spring Cloud Gateway application.

Its responsibilities include:

- Accepting incoming HTTP requests.
- Matching requests against configured routes.
- Forwarding requests to the appropriate backend service.
- Applying gateway filters.
- Returning backend responses to clients.

The gateway listens on port `8080` by default.

### Order Service

The `ag-order-service` module provides order-related functionality.

It exposes an endpoint for retrieving order information.

The service listens on port `8081` by default.

### Inventory Service

The `ag-inventory-service` module provides inventory-related functionality.

It exposes an endpoint for retrieving inventory information.

The service listens on port `8082` by default.

### Integration Tests

The `ag-integration-tests` module verifies that the gateway routes requests correctly.

It starts the backend services and launches the gateway in a separate process.

This isolates the reactive gateway application from the Spring MVC dependencies used by the backend services.

---

## Gateway Configuration

The gateway routes are configured in `application.yml`.

```yaml
server:
  port: 8080

spring:
  application:
    name: ag-gateway-server

  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: order-service
              uri: ${services.order.url:http://localhost:8081}
              predicates:
                - Path=/api/orders/**
              filters:
                - StripPrefix=1
                - AddResponseHeader=X-Gateway-Route,orders

            - id: inventory-service
              uri: ${services.inventory.url:http://localhost:8082}
              predicates:
                - Path=/api/inventory/**
              filters:
                - StripPrefix=1
                - AddResponseHeader=X-Gateway-Route,inventory
```

This configuration defines two routes.

### Order Service Route

```yaml
- id: order-service
  uri: ${services.order.url:http://localhost:8081}
  predicates:
    - Path=/api/orders/**
  filters:
    - StripPrefix=1
    - AddResponseHeader=X-Gateway-Route,orders
```

The route has three important components.

**Route identifier**

```yaml
id: order-service
```

Identifies the route within the gateway configuration.

**Destination URI**

```yaml
uri: ${services.order.url:http://localhost:8081}
```

Defines the backend service receiving matching requests.

The configuration uses a property placeholder with a default value.

If `services.order.url` is provided, that value is used. Otherwise, the gateway forwards requests to `http://localhost:8081`.

**Path predicate**

```yaml
predicates:
  - Path=/api/orders/**
```

Matches incoming requests whose paths begin with `/api/orders/`.

For example:

```text
/api/orders/ORD-1001
/api/orders/ORD-1002
/api/orders/ORD-1003
```

All these requests match the order service route.

### Inventory Service Route

```yaml
- id: inventory-service
  uri: ${services.inventory.url:http://localhost:8082}
  predicates:
    - Path=/api/inventory/**
  filters:
    - StripPrefix=1
    - AddResponseHeader=X-Gateway-Route,inventory
```

This route matches requests beginning with `/api/inventory/` and forwards them to the inventory service.

For example:

```text
/api/inventory/SKU-1001
/api/inventory/SKU-1002
```

The gateway therefore distinguishes between order and inventory requests using their URL paths.

---

## Understanding Route Predicates

A route predicate determines whether an incoming request should be handled by a particular route.

This example uses the `Path` predicate.

For the order service:

```yaml
predicates:
  - Path=/api/orders/**
```

The predicate matches requests such as:

```text
/api/orders/ORD-1001
/api/orders/ORD-2001
/api/orders/ORD-3001
```

For the inventory service:

```yaml
predicates:
  - Path=/api/inventory/**
```

The predicate matches requests such as:

```text
/api/inventory/SKU-1001
/api/inventory/SKU-2001
```

The gateway evaluates configured predicates to determine which route should handle an incoming request.

Conceptually:

```text
Incoming Request
       │
       ▼
  Evaluate Predicates
       │
       ├── /api/orders/** ───────► Order Service
       │
       ├── /api/inventory/** ────► Inventory Service
       │
       └── No Match ─────────────► HTTP 404
```

---

## Understanding Gateway Filters

Gateway filters allow requests and responses to be modified during gateway processing.

This example uses two filters:

- `StripPrefix`
- `AddResponseHeader`

### StripPrefix Filter

The `StripPrefix` filter removes a specified number of path segments before forwarding the request to the backend service.

The example uses:

```yaml
filters:
  - StripPrefix=1
```

Consider this incoming request:

```text
/api/orders/ORD-1001
```

The first path segment is:

```text
api
```

After applying `StripPrefix=1`, the forwarded request becomes:

```text
/orders/ORD-1001
```

The gateway removes the gateway-specific `/api` prefix while preserving the remaining path.

The transformation looks like:

```text
Incoming Request
/api/orders/ORD-1001
        │
        ▼
  StripPrefix=1
        │
        ▼
Forwarded Request
/orders/ORD-1001
```

This allows the gateway to expose a consistent external API structure without requiring backend services to use the same URL prefix.

### AddResponseHeader Filter

The `AddResponseHeader` filter adds a header to the response returned to the client.

The order service route uses:

```yaml
- AddResponseHeader=X-Gateway-Route,orders
```

The inventory service route uses:

```yaml
- AddResponseHeader=X-Gateway-Route,inventory
```

For an order request, the gateway adds:

```http
X-Gateway-Route: orders
```

For an inventory request, it adds:

```http
X-Gateway-Route: inventory
```

This makes it possible to verify which gateway route handled a request.

Conceptually:

```text
Backend Response
       │
       ▼
Gateway Response Filter
       │
       ▼
Add X-Gateway-Route Header
       │
       ▼
Client Response
```

These headers are useful for demonstrating gateway filtering and should not be treated as a substitute for proper observability or security controls.

---

## Request Routing Flow

The order service request follows this flow:

```text
Client
  │
  │ GET /api/orders/ORD-1001
  ▼
API Gateway
  │
  ▼
Path Predicate
/api/orders/**
  │
  ▼
StripPrefix=1
  │
  ▼
GET /orders/ORD-1001
  │
  ▼
Order Service
  │
  ▼
Order Response
  │
  ▼
AddResponseHeader
X-Gateway-Route: orders
  │
  ▼
Client
```

The inventory service follows a similar flow:

```text
Client
  │
  │ GET /api/inventory/SKU-1001
  ▼
API Gateway
  │
  ▼
Path Predicate
/api/inventory/**
  │
  ▼
StripPrefix=1
  │
  ▼
GET /inventory/SKU-1001
  │
  ▼
Inventory Service
  │
  ▼
Inventory Response
  │
  ▼
AddResponseHeader
X-Gateway-Route: inventory
  │
  ▼
Client
```

The gateway handles route selection and forwarding, while the backend services remain responsible for their respective business functionality.

---

## Configuring Backend Service URLs

The gateway uses externalized configuration for backend service URLs.

For example:

```yaml
uri: ${services.order.url:http://localhost:8081}
```

This provides two benefits:

1. The gateway has a default destination for local development.
2. The destination can be overridden without modifying the gateway configuration.

The same applies to the inventory service:

```yaml
uri: ${services.inventory.url:http://localhost:8082}
```

For example, the gateway can be started with different backend URLs:

```bash
java -jar ag-gateway-server.jar \
  --services.order.url=http://localhost:9081 \
  --services.inventory.url=http://localhost:9082
```

This makes the gateway easier to configure across different environments.

---

## Why the Gateway Runs Separately

The gateway uses Spring WebFlux, while the backend services use Spring MVC.

Spring Cloud Gateway WebFlux checks its application classpath for incompatible Spring MVC dependencies.

If Spring MVC is present on the gateway application's classpath, gateway startup can fail with an error indicating that Spring MVC is incompatible with Spring Cloud Gateway.

This example therefore runs the gateway in a separate JVM process during integration testing.

The integration-test JVM starts the two Spring MVC backend applications.

The gateway is launched using its own generated dependency classpath.

Conceptually:

```text
Integration Test JVM
│
├── Order Service
│     └── Spring MVC
│
└── Inventory Service
      └── Spring MVC


Separate Gateway JVM
│
└── Gateway Server
      └── Spring WebFlux
```

This preserves the dependency isolation required by the two web application stacks.

The gateway's dependency classpath is generated using the Maven Dependency Plugin.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <id>build-gateway-classpath</id>
            <phase>prepare-package</phase>
            <goals>
                <goal>build-classpath</goal>
            </goals>
            <configuration>
                <outputFile>${project.build.directory}/gateway-classpath.txt</outputFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

The generated classpath is then used when launching the gateway process.

This approach allows the integration tests to verify real HTTP communication between independently running applications without introducing incompatible dependencies into a shared application classpath.

---

## Testing API Gateway Routing

The example uses Spring Boot test support, JUnit, AssertJ, and Spring's `RestClient`.

The integration tests start:

1. The order service on a randomly assigned port.
2. The inventory service on a randomly assigned port.
3. The gateway on a randomly assigned port.

The gateway receives the actual backend service URLs as startup arguments.

This avoids hard-coded port dependencies during integration testing.

### Testing Order Service Routing

The test sends a request through the gateway:

```java
ResponseEntity<OrderResponse> response = gatewayClient.get().uri("/api/orders/ORD-1001").retrieve()
        .toEntity(OrderResponse.class);
```

The test verifies:

- The response is successful.
- The response body is present.
- The response comes from the order service.
- The requested order identifier is preserved.
- The gateway response header identifies the order route.

For example:

```java
assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
assertThat(response.getBody()).isNotNull();
assertThat(response.getBody().service()).isEqualTo("order-service");
assertThat(response.getBody().orderId()).isEqualTo("ORD-1001");
assertThat(response.getHeaders().getFirst("X-Gateway-Route")).isEqualTo("orders");
```

This verifies that the gateway forwards order requests to the correct backend service.

### Testing Inventory Service Routing

The inventory test sends:

```java
ResponseEntity<InventoryResponse> response = gatewayClient.get().uri("/api/inventory/SKU-1001").retrieve()
        .toEntity(InventoryResponse.class);
```

It verifies that:

- The response is successful.
- The inventory service handled the request.
- The requested SKU is preserved.
- The expected available quantity is returned.
- The gateway response header identifies the inventory route.

For example:

```java
assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
assertThat(response.getBody()).isNotNull();
assertThat(response.getBody().service()).isEqualTo("inventory-service");
assertThat(response.getBody().sku()).isEqualTo("SKU-1001");
assertThat(response.getBody().availableQuantity()).isEqualTo(100);
assertThat(response.getHeaders().getFirst("X-Gateway-Route")).isEqualTo("inventory");
```

### Testing Unmatched Routes

The gateway should not forward requests that do not match any configured route.

For example:

```text
GET /api/customers/CUST-1001
```

There is no customer service route in this example.

The gateway therefore returns:

```http
HTTP/1.1 404 Not Found
```

The test uses `RestClient.exchange()`:

```java
ResponseEntity<String> response = gatewayClient.get().uri("/api/customers/CUST-1001")
        .exchange((request, clientResponse) ->
                ResponseEntity.status(clientResponse.getStatusCode())
                        .body(clientResponse.bodyTo(String.class)));

assertThat(response.getStatusCode().value()).isEqualTo(404);
```

`exchange()` is useful here because it provides direct access to the response without applying the default error handling used by `retrieve()`.

By default, `retrieve()` throws an exception for HTTP 4xx and 5xx responses.

The test therefore uses `exchange()` to inspect the expected `404` response directly.

---

## Integration Test Lifecycle

The integration test manages the lifecycle of the backend applications and gateway process.

The startup sequence is:

```text
@BeforeAll
    │
    ▼
Start Order Service
    │
    ▼
Start Inventory Service
    │
    ▼
Retrieve Assigned Ports
    │
    ▼
Find Gateway Port
    │
    ▼
Launch Gateway Process
    │
    ▼
Wait for Gateway Availability
    │
    ▼
Execute Integration Tests
```

The services use random ports:

```java
orderServiceContext = new SpringApplicationBuilder(OrderServiceApplication.class)
        .web(WebApplicationType.SERVLET)
        .run("--server.port=0");
```

Passing `--server.port=0` as a command-line argument ensures that the random-port setting takes precedence over the ports configured in `application.yml`.

The assigned port is retrieved from the running web application context.

After the tests finish, the gateway process and backend application contexts are shut down.

```text
@AfterAll
    │
    ▼
Stop Gateway Process
    │
    ▼
Close Inventory Service
    │
    ▼
Close Order Service
```

This prevents application processes and server ports from being left running after the tests complete.

---

## API Gateway vs Direct Service Communication

The difference between direct service communication and gateway-based communication can be summarized as follows.

### Direct Communication

```text
Client
  │
  ├──────────────► Order Service
  │
  └──────────────► Inventory Service
```

The client must know the address of each backend service.

### Gateway-Based Communication

```text
Client
  │
  ▼
API Gateway
  │
  ├──────────────► Order Service
  │
  └──────────────► Inventory Service
```

The client communicates through a single entry point.

The gateway handles routing to the appropriate backend service.

---

## API Gateway vs Service Discovery

API Gateway and service discovery solve different problems.

**API Gateway** provides a single entry point and routes incoming requests to backend services.

**Service Discovery** allows applications to locate available service instances dynamically.

Without service discovery, a gateway can route requests using configured service URLs:

```text
API Gateway
    │
    ▼
http://localhost:8081
    │
    ▼
Order Service
```

With service discovery and load balancing, the gateway can use a logical service identifier and resolve available instances dynamically.

For example:

```text
API Gateway
    │
    ▼
Order Service
    │
    ▼
Service Discovery
    │
    ├──────────────► Order Instance 1
    │
    └──────────────► Order Instance 2
```

This example focuses on explicit backend URLs.

Service discovery and load balancing are explored separately in other Spring Cloud examples.

---

## Running the Example

From the repository root, build the API Gateway example and its required modules:

```bash
mvn -pl 09-spring-cloud/api-gateway -am package
```

Run the backend services independently:

```bash
mvn -pl 09-spring-cloud/api-gateway/ag-order-service spring-boot:run
```

```bash
mvn -pl 09-spring-cloud/api-gateway/ag-inventory-service spring-boot:run
```

Run the gateway:

```bash
mvn -pl 09-spring-cloud/api-gateway/ag-gateway-server spring-boot:run
```

The default application ports are:

| Application       | Port |
|-------------------|-----:|
| Gateway Server    | 8080 |
| Order Service     | 8081 |
| Inventory Service | 8082 |

---

## Testing the Example

Run the integration tests from the repository root:

```bash
mvn -pl 09-spring-cloud/api-gateway/ag-integration-tests -am verify
```

Alternatively, run the entire API Gateway example:

```bash
mvn -pl 09-spring-cloud/api-gateway -am verify
```

The integration tests verify routing to both backend services and the handling of unmatched requests.

---

## Example Requests

Once the applications are running, the following requests can be sent through the gateway.

### Retrieve an Order

```bash
curl -i http://localhost:8080/api/orders/ORD-1001
```

Expected response header:

```http
X-Gateway-Route: orders
```

### Retrieve Inventory

```bash
curl -i http://localhost:8080/api/inventory/SKU-1001
```

Expected response header:

```http
X-Gateway-Route: inventory
```

### Access an Unmatched Route

```bash
curl -i http://localhost:8080/api/customers/CUST-1001
```

Expected status:

```http
HTTP/1.1 404 Not Found
```

---

## Key Takeaways

- An API Gateway provides a single entry point to multiple backend services.
- Spring Cloud Gateway provides routing and filtering capabilities.
- Spring Cloud Gateway WebFlux uses a reactive web stack.
- Routes define the destination for matching requests.
- Predicates determine whether an incoming request matches a route.
- Filters can modify requests and responses during gateway processing.
- `Path` predicates allow routing based on request paths.
- `StripPrefix` removes path segments before forwarding requests.
- `AddResponseHeader` adds headers to gateway responses.
- Externalized configuration makes backend service URLs configurable.
- A gateway can communicate with backend services using HTTP even when they use different Spring web stacks.
- Gateway WebFlux and Spring MVC must be isolated from one another at the application classpath level.
- Integration tests can verify real routing behavior across independently running applications.
- Unmatched routes return HTTP 404 when no configured route matches.

---

## Next Example

The next example will explore **Load Balancing** in Spring Cloud, including distributing requests across multiple instances of a service, client-side load balancing, and Spring Cloud LoadBalancer integration.
