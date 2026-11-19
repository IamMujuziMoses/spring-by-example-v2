# Declarative HTTP Clients

This example demonstrates **declarative HTTP clients** in Spring.

It focuses on how a Spring application can communicate with another HTTP service through a Java interface rather than manually constructing HTTP requests.

The example uses Spring's HTTP Service Clients, `@HttpExchange`, `@GetExchange`, `RestClient`, and `HttpServiceProxyFactory` to implement communication between an Order Service and an Inventory Service.

It demonstrates how declarative HTTP clients simplify service-to-service communication in a Spring Cloud application.

## What This Example Demonstrates

- What declarative HTTP clients are.
- Service-to-service communication over HTTP.
- Spring HTTP Service Clients.
- The `@HttpExchange` annotation.
- The `@GetExchange` annotation.
- Defining HTTP clients using Java interfaces.
- Creating HTTP client proxies with `HttpServiceProxyFactory`.
- Using `RestClientAdapter` to integrate `RestClient` with HTTP Service Clients.
- Configuring a remote service's base URL.
- Separating service-specific request and response models.
- Integrating declarative HTTP clients into a Spring service.
- Testing service logic with unit tests.
- Testing communication between independently running Spring Boot applications.

---

## What Are Declarative HTTP Clients?

A declarative HTTP client provides an interface-based way of defining HTTP communication with another service.

Instead of manually creating HTTP requests, specifying URLs, setting headers, and processing responses in application code, developers define an interface that describes the remote HTTP operations.

Spring creates an implementation of that interface at runtime.

For example:

```java
@HttpExchange("/api/inventory")
public interface InventoryClient {

    @GetExchange("/{productId}")
    InventoryResponse getInventory(@PathVariable("productId") String productId);
}
```

The interface describes an HTTP GET operation.

Spring generates a proxy that performs the actual HTTP request when `getInventory()` is invoked.

Conceptually:

```text
Application Code
      │
      ▼
InventoryClient Interface
      │
      ▼
Generated HTTP Client Proxy
      │
      ▼
HTTP Request
      │
      ▼
Inventory Service
      │
      ▼
HTTP Response
      │
      ▼
InventoryResponse
```

The application interacts with a Java interface while Spring handles the underlying HTTP communication.

---

## What Are Spring HTTP Service Clients?

Spring HTTP Service Clients provide support for defining HTTP services as Java interfaces.

They use annotations to describe HTTP operations and a proxy factory to create implementations of those interfaces.

The main components used in this example are:

| Component                 | Responsibility                                                   |
|---------------------------|------------------------------------------------------------------|
| `@HttpExchange`           | Defines common HTTP exchange configuration, such as a base path. |
| `@GetExchange`            | Declares an HTTP GET operation.                                  |
| `RestClient`              | Performs synchronous HTTP requests.                              |
| `RestClientAdapter`       | Adapts `RestClient` for use with HTTP Service Clients.           |
| `HttpServiceProxyFactory` | Creates implementations of HTTP service interfaces.              |

Together, these components provide a declarative approach to HTTP communication.

---

## Example Architecture

This example contains two independent Spring Boot applications:

- **Order Service**: Accepts order requests and checks product availability.
- **Inventory Service**: Provides available inventory quantities for products.

The Order Service communicates with the Inventory Service through a declarative HTTP client.

```text
                 HTTP POST
              /api/orders
                    │
                    ▼
          ┌───────────────────┐
          │   Order Service   │
          │                   │
          │  OrderController  │
          │        │          │
          │        ▼          │
          │   OrderService    │
          │        │          │
          │        ▼          │
          │  InventoryClient  │
          │        │          │
          └────────┼──────────┘
                   │
                   │ HTTP GET
                   │ /api/inventory/{productId}
                   ▼
          ┌───────────────────┐
          │ Inventory Service │
          │                   │
          │InventoryController│
          │        │          │
          │        ▼          │
          │ InventoryService  │
          │        │          │
          │        ▼          │
          │ InventoryResponse │
          │                   │
          └───────────────────┘
```

The Inventory Service does not depend on the Order Service.

The Order Service only depends on the HTTP contract exposed by the Inventory Service.

This keeps the two applications independently deployable.

---

## Project Structure

The example is organized into three Maven modules, the three modules have separate responsibilities:

| Module                  | Responsibility                                             |
|-------------------------|------------------------------------------------------------|
| `dhc-inventory-service` | Exposes inventory information through a REST API.          |
| `dhc-order-service`     | Processes orders and communicates with Inventory Service.  |
| `dhc-integration-tests` | Contains unit tests and integration tests for the example. |

---

## Inventory Service

The Inventory Service exposes an HTTP endpoint that allows other services to retrieve the available quantity of a product.

### Inventory Endpoint

The service exposes:

```http
GET /api/inventory/{productId}
```

For example:

```http
GET /api/inventory/SKU-1001
```

A successful response looks like:

```json
{
  "productId": "SKU-1001",
  "availableQuantity": 25
}
```

### Inventory Response

The response is represented by a Java record:

```java
public record InventoryResponse(String productId, int availableQuantity) {
}
```

This record represents the inventory information returned by the service.

### Inventory Service Implementation

The service maintains a small in-memory inventory:

```java
@Service
public class InventoryService {

    private final Map<String, Integer> inventory = Map.of("SKU-1001", 25, "SKU-2002", 10, "SKU-3003", 5);

    public InventoryResponse lookup(String productId) {
        int availableQuantity = inventory.getOrDefault(productId, 0);

        return new InventoryResponse(productId, availableQuantity);
    }
}
```

The `lookup()` method retrieves the available quantity for a product.

If a product does not exist, the service returns zero available units.

This keeps the example focused on HTTP communication rather than database integration.

### Inventory Controller

The controller exposes the service operation through a REST endpoint:

```java
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public InventoryResponse getInventory(@PathVariable String productId) {
        return inventoryService.lookup(productId);
    }
}
```

The controller receives the product identifier and delegates the lookup to `InventoryService`.

The resulting `InventoryResponse` is serialized into JSON by Spring MVC.

---

## Defining the Declarative HTTP Client

The Order Service needs to retrieve inventory information from the Inventory Service.

Instead of manually constructing HTTP requests inside `OrderService`, the example defines an interface named `InventoryClient`.

```java
@HttpExchange("/api/inventory")
public interface InventoryClient {

    @GetExchange("/{productId}")
    InventoryResponse getInventory(@PathVariable("productId") String productId);
}
```

This interface describes the remote HTTP operation.

### Understanding `@HttpExchange`

The `@HttpExchange` annotation defines common HTTP exchange configuration for the interface.

```java
@HttpExchange("/api/inventory")
```

It establishes the base path for the operations declared in the interface.

The method-level mapping is then combined with this path.

### Understanding `@GetExchange`

The `@GetExchange` annotation declares an HTTP GET operation.

```java
@GetExchange("/{productId}")
```

When the method is invoked, Spring performs a GET request to the corresponding URL.

For example:

```java
inventoryClient.getInventory("SKU-1001");
```

results in a request equivalent to:

```http
GET /api/inventory/SKU-1001
```

### Understanding `@PathVariable`

The `@PathVariable` annotation binds the method argument to the path variable declared in the exchange mapping.

```java
@PathVariable("productId") String productId
```

Spring substitutes the supplied product identifier into the request URL.

### Why Use an Interface?

The interface separates the HTTP contract from the implementation details of making the request.

The application does not need to know:

- How the HTTP request is constructed.
- How the request is executed.
- How the response is deserialized.
- How the client proxy is implemented.

It only needs to know the interface and its method signatures.

---

## Creating the HTTP Client Proxy

Defining the interface is not enough.

Spring needs an HTTP client implementation that can execute the operations declared by the interface.

This example uses:

- `RestClient`
- `RestClientAdapter`
- `HttpServiceProxyFactory`

These components are configured in `InventoryClientConfiguration`.

```java
@Configuration
public class InventoryClientConfiguration {

    @Bean
    public InventoryClient inventoryClient(RestClient.Builder builder, @Value("${inventory.service.url}") String inventoryServiceUrl) {
        RestClient restClient = builder.baseUrl(inventoryServiceUrl).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(InventoryClient.class);
    }
}
```

### Step 1: Create a RestClient

```java
RestClient restClient = builder.baseUrl(inventoryServiceUrl).build();
```

`RestClient` is Spring's synchronous HTTP client.

It provides the underlying mechanism for executing HTTP requests.

The base URL identifies the Inventory Service.

For example:

```text
http://localhost:8082
```

The client combines this base URL with the paths declared in `InventoryClient`.

### Step 2: Create a RestClientAdapter

```java
RestClientAdapter adapter = RestClientAdapter.create(restClient);
```

`RestClientAdapter` adapts the `RestClient` instance so that it can be used by Spring HTTP Service Clients.

It connects the HTTP execution mechanism with the declarative interface.

### Step 3: Create HttpServiceProxyFactory

```java
HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
```

`HttpServiceProxyFactory` creates proxy implementations for HTTP service interfaces.

It uses the configured adapter to execute the HTTP operations.

### Step 4: Create the InventoryClient Proxy

```java
return factory.createClient(InventoryClient.class);
```

Spring creates an implementation of `InventoryClient`.

The returned object is a proxy that knows how to execute the HTTP operations declared by the interface.

The application can now inject `InventoryClient` into other Spring-managed components.

---

## Configuring the Inventory Service URL

The Inventory Service URL is externalized into application configuration.

The Order Service uses:

```yaml
inventory:
  service:
    url: ${INVENTORY_SERVICE_URL:http://localhost:8082}
```

This configuration provides:

- A default URL for local development.
- An environment variable for overriding the URL.
- Separation between application code and deployment-specific configuration.

For example, the default URL is:

```text
http://localhost:8082
```

The URL can be changed without modifying the Java implementation:

```bash
INVENTORY_SERVICE_URL=http://inventory-service:8082
```

This is useful when deploying services into different environments.

The Order Service does not need to know whether the Inventory Service is running locally, in a container, or on another host.

It only needs the configured service URL.

---

## Using the Declarative HTTP Client

Once the client has been configured, it can be injected into the Order Service.

```java
@Service
public class OrderService {

    private final InventoryClient inventoryClient;

    public OrderService(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    public OrderResponse process(OrderRequest request) {
        InventoryResponse inventory = inventoryClient.getInventory(request.productId());

        if (inventory == null) {
            throw new IllegalStateException("Inventory Service returned an empty response");
        }

        String status = inventory.availableQuantity() >= request.quantity() ? "ACCEPTED" : "REJECTED";

        return new OrderResponse(request.orderId(), request.productId(), request.quantity(), inventory.availableQuantity(),
                status);
    }
}
```

The important operation is:

```java
InventoryResponse inventory = inventoryClient.getInventory(request.productId());
```

The Order Service invokes a Java method.

Behind the scenes, Spring's generated proxy:

1. Resolves the configured Inventory Service base URL.
2. Builds the HTTP GET request.
3. Sends the request using `RestClient`.
4. Receives the HTTP response.
5. Deserializes the response into `InventoryResponse`.
6. Returns the response to `OrderService`.

The Order Service can then continue processing the order without manually handling the HTTP exchange.

---

## Service-Specific Response Models

Both services define their own `InventoryResponse` record.

The Inventory Service has:

```java
public record InventoryResponse(String productId, int availableQuantity) {
}
```

The Order Service has:

```java
public record InventoryResponse(String productId, int availableQuantity) {
}
```

Although the records have the same structure, they belong to separate applications.

The Order Service does not import a Java class from the Inventory Service to represent the remote response.

Instead, it defines its own representation of the HTTP response contract.

This is an important consideration in distributed systems.

### Why Keep the Models Separate?

Sharing Java model classes directly between independently deployable services can introduce unnecessary compile-time coupling.

For example, a change to an internal Java package in Inventory Service should not require Order Service to depend on that service's implementation module.

The HTTP contract is the integration boundary.

Each service owns its internal representation of the data it sends or receives.

Conceptually:

```text
Inventory Service
       │
       ▼
InventoryResponse
       │
       ▼
     JSON
       │
       ▼
Order Service
       │
       ▼
InventoryResponse
```

The services communicate through the serialized HTTP representation rather than sharing an implementation class.

For larger systems, API contracts can also be documented and validated using tools such as OpenAPI or schema-based contract testing.

---

## Order Processing Flow

The example exposes an order creation endpoint:

```http
POST /api/orders
```

A request looks like:

```json
{
  "orderId": "ORD-1001",
  "productId": "SKU-1001",
  "quantity": 2
}
```

The Order Service retrieves inventory information before determining the order status.

The processing flow is:

```text
HTTP POST /api/orders
          │
          ▼
    OrderController
          │
          ▼
      OrderService
          │
          ▼
    InventoryClient
          │
          ▼
 HTTP GET /api/inventory/SKU-1001
          │
          ▼
   Inventory Service
          │
          ▼
 InventoryResponse
          │
          ▼
    OrderService
          │
          ▼
    Check Quantity
          │
          ▼
   OrderResponse
```

### Sufficient Inventory

Suppose the requested quantity is 2 and the Inventory Service reports 25 available units.

The order is accepted.

```json
{
  "orderId": "ORD-1001",
  "productId": "SKU-1001",
  "quantity": 2,
  "availableQuantity": 25,
  "status": "ACCEPTED"
}
```

### Insufficient Inventory

Suppose the requested quantity is 10 and the Inventory Service reports only 5 available units.

The order is rejected.

```json
{
  "orderId": "ORD-1002",
  "productId": "SKU-3003",
  "quantity": 10,
  "availableQuantity": 5,
  "status": "REJECTED"
}
```

### Unknown Product

If the requested product does not exist, the Inventory Service returns zero available units.

The Order Service rejects the order.

```json
{
  "orderId": "ORD-1003",
  "productId": "SKU-9999",
  "quantity": 1,
  "availableQuantity": 0,
  "status": "REJECTED"
}
```

These scenarios demonstrate how one Spring service can use a declarative HTTP client to obtain information from another service before completing its own business operation.

---

## HTTP Client Configuration and Timeouts

The Order Service also configures HTTP client timeout properties:

```yaml
spring:
  http:
    clients:
      connect-timeout: 2s
      read-timeout: 5s
```

Timeouts are important when communicating with remote services.

### Connection Timeout

The connection timeout controls how long the client waits when establishing a connection to the remote service.

### Read Timeout

The read timeout controls how long the client waits for data from the remote service after establishing the connection.

Without appropriate timeouts, an application may spend too long waiting for an unresponsive downstream service.

The exact configuration supported depends on the HTTP client and Spring Boot version. It is important to verify that the configured properties are applied to the client used by the application.

---

## Declarative HTTP Clients vs Manual HTTP Requests

Without declarative HTTP clients, an application can use `RestClient` directly.

For example:

```java
InventoryResponse inventory = restClient.get()
        .uri("/api/inventory/{productId}", productId)
        .retrieve()
        .body(InventoryResponse.class);
```

This approach is perfectly valid.

However, the application must work directly with the HTTP client API whenever it needs to make a remote request.

With a declarative HTTP client, the same operation is represented by an interface method:

```java
InventoryResponse inventory = inventoryClient.getInventory(productId);
```

The difference is primarily in how the HTTP interaction is expressed and organized.

| Manual HTTP Requests                                           | Declarative HTTP Clients                                      |
|----------------------------------------------------------------|---------------------------------------------------------------|
| Application code invokes HTTP client methods directly.         | Application code invokes interface methods.                   |
| Request details are expressed in the calling code.             | Request details are declared through annotations.             |
| HTTP operations can be repeated across application components. | HTTP operations can be centralized in an interface.           |
| The application directly depends on the HTTP client API.       | Application services depend on the declared client interface. |

Declarative HTTP clients do not eliminate HTTP communication. They provide a more structured abstraction over it.

---

## Spring HTTP Service Clients vs OpenFeign

Spring Cloud OpenFeign is another approach to declarative HTTP communication.

Both Spring HTTP Service Clients and OpenFeign allow developers to define remote HTTP operations through interfaces.

However, they are different technologies.

| Spring HTTP Service Clients                                      | Spring Cloud OpenFeign                 |
|------------------------------------------------------------------|----------------------------------------|
| Provided by the Spring Framework.                                | Provided by Spring Cloud OpenFeign.    |
| Uses Spring HTTP exchange annotations.                           | Uses Feign client annotations.         |
| Supports `RestClient`, `WebClient`, and `RestTemplate` adapters. | Uses the Feign client abstraction.     |
| Uses `HttpServiceProxyFactory` to create client proxies.         | Uses Feign's client infrastructure.    |
| Can be configured directly through Spring Framework APIs.        | Integrates with Spring Cloud features. |

This example uses Spring HTTP Service Clients to demonstrate the HTTP client abstraction available directly in the Spring Framework.

OpenFeign can be explored separately when demonstrating Spring Cloud's Feign integration.

---

## Declarative HTTP Clients in Microservices

In a microservices architecture, services often need to communicate with other services to complete business operations.

For example:

```text
Order Service
     │
     ├──────────► Inventory Service
     │
     ├──────────► Payment Service
     │
     └──────────► Customer Service
```

Each service exposes an API that other services can consume.

Declarative HTTP clients provide a convenient way to define these service-to-service interactions.

However, using declarative HTTP clients does not automatically provide:

- Service discovery.
- Client-side load balancing.
- Retry mechanisms.
- Circuit breakers.
- Distributed tracing.
- Fault tolerance.

These capabilities require additional configuration and supporting technologies.

For example, a client configured with a fixed base URL does not automatically discover service instances or distribute requests across them.

Those concerns can be explored independently.

---

## Running the Example

The example consists of two Spring Boot applications.

The Inventory Service runs on port `8082` by default.

The Order Service runs on port `8081` by default.

### Start Inventory Service

From the repository root:

```bash
mvn -pl 09-spring-cloud/declarative-http-clients/dhc-inventory-service spring-boot:run
```

### Start Order Service

In another terminal:

```bash
mvn -pl 09-spring-cloud/declarative-http-clients/dhc-order-service spring-boot:run
```

The Order Service uses the default Inventory Service URL:

```text
http://localhost:8082
```

### Retrieve Inventory

Send a request to Inventory Service:

```bash
curl http://localhost:8082/api/inventory/SKU-1001
```

Expected response:

```json
{
  "productId": "SKU-1001",
  "availableQuantity": 25
}
```

### Create an Order

Send an order request to Order Service:

```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-1001",
    "productId": "SKU-1001",
    "quantity": 2
  }'
```

Expected response:

```json
{
  "orderId": "ORD-1001",
  "productId": "SKU-1001",
  "quantity": 2,
  "availableQuantity": 25,
  "status": "ACCEPTED"
}
```

---

## Running the Tests

The example centralizes its tests in the `dhc-integration-tests` module.

Run the tests from the repository root:

```bash
mvn -pl 09-spring-cloud/declarative-http-clients/dhc-integration-tests -am clean verify
```

The `-am` option also builds the required modules in the Maven reactor.

The command runs:

- Inventory Service unit tests.
- Order Service unit tests.
- Declarative HTTP client integration tests.

---

## Key Takeaways

- Declarative HTTP clients provide an interface-based approach to HTTP communication.
- Spring HTTP Service Clients allow HTTP operations to be defined using Java interfaces.
- `@HttpExchange` defines common HTTP exchange configuration.
- `@GetExchange` declares an HTTP GET operation.
- `RestClient` performs the underlying synchronous HTTP communication.
- `RestClientAdapter` integrates `RestClient` with Spring HTTP Service Clients.
- `HttpServiceProxyFactory` creates implementations of HTTP service interfaces.
- The generated proxy handles the HTTP request and response exchange.
- Service-specific response models help maintain independent service boundaries.
- Externalized configuration allows remote service URLs to change between environments.
- Unit tests verify service logic independently of HTTP communication.
- Integration tests verify actual communication between Spring Boot applications.
- Declarative HTTP clients simplify how service-to-service HTTP interactions are expressed, but do not automatically provide service discovery, load balancing, retries, or circuit breakers.

---

## Next Example

The next example will explore **Messaging** in Spring Cloud, including asynchronous communication between services and the role of messaging systems in distributed applications.
