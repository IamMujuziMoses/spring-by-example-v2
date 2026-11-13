# Service Discovery

This example demonstrates **service discovery with Spring Cloud Netflix Eureka**.

It focuses on how independently running Spring Boot applications register themselves with a Eureka Server, how other applications discover registered services, and how service discovery enables communication without hardcoding service instance addresses.

The example consists of three independent applications:

- **Discovery Server** — provides the Eureka service registry.
- **Order Service** — registers itself with Eureka and exposes an order endpoint.
- **Order Client** — discovers Order Service through Eureka and sends HTTP requests to it.

The example also includes an integration test that starts all three applications and verifies the complete service discovery flow.

## What This Example Demonstrates

- What service discovery means in distributed applications.
- The role of a service registry.
- Spring Cloud Netflix Eureka Server.
- Spring Cloud Netflix Eureka Client.
- Registering services with Eureka.
- Discovering services using `DiscoveryClient`.
- Communicating with discovered service instances.
- Using Spring's `RestClient` for HTTP communication.
- Configuring service registration and registry fetching.
- Running multiple Spring Boot applications independently.
- Testing service discovery across multiple application contexts.
- Using dynamically allocated ports during integration testing.

---

## What Is Service Discovery?

Service discovery is a mechanism that allows applications to locate other services dynamically without requiring their network addresses to be configured manually.

In a distributed system, services may run on different hosts, ports, containers, or infrastructure environments.

Their locations can change as instances are deployed, restarted, scaled, or moved.

Without service discovery, an application might need to maintain a list of service addresses:

```text
Order Client
     |
     |-- http://192.168.1.10:8081
     |-- http://192.168.1.11:8081
     |-- http://192.168.1.12:8081
```

This approach creates additional maintenance because the client must know where every service instance is running.

Service discovery addresses this problem by introducing a service registry.

Instead of maintaining service addresses manually, applications register their locations with the registry, and clients query it when they need to communicate with another service.

---

## What Is a Service Registry?

A service registry is a central component that maintains information about available service instances.

It typically maintains information such as:

- Service names.
- Service instance addresses.
- Hostnames and ports.
- Instance availability and registration status.
- Other metadata associated with service instances.

In this example, **Eureka Server** acts as the service registry.

The Order Service registers itself with Eureka, while the Order Client queries Eureka to discover available Order Service instances.

Conceptually:

```text
              Eureka Server
              (Service Registry)
                     |
          +----------+----------+
          |                     |
          ▼                     ▼
     Order Service         Order Client
       Registers            Discovers
       itself               Order Service
```

The registry provides a shared location where applications can publish and retrieve service instance information.

---

## Spring Cloud Netflix Eureka

This example uses **Netflix Eureka**, integrated into Spring applications through Spring Cloud Netflix.

Eureka provides two primary components.

### Eureka Server

Eureka Server provides the service registry.

It receives registration information from Eureka clients and makes registered service instances available for discovery.

The Discovery Server application enables Eureka Server using:

```java
@EnableEurekaServer
@SpringBootApplication
public class DiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
```

The `@EnableEurekaServer` annotation configures the application to act as a Eureka Server.

### Eureka Client

A Eureka Client is an application that communicates with the Eureka Server.

A Eureka Client can:

- Register its application instance with Eureka.
- Fetch the service registry.
- Discover other registered services.
- Deregister itself when it shuts down.

Both Order Service and Order Client use the Eureka Client starter:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

Spring Cloud automatically configures the Eureka client infrastructure when the appropriate dependency is available and Eureka client configuration is enabled.

---

## Example Architecture

The example contains three independent Spring Boot applications.

```text
                         ┌──────────────────────┐
                         │                      │
                         │    Discovery Server  │
                         │                      │
                         │     Eureka Server    │
                         │      Port: 8761      │
                         │                      │
                         └──────────┬───────────┘
                                    │
                         Service Registry
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
              Registration                    Registration
                    │                               │
                    ▼                               ▼
       ┌──────────────────────┐        ┌──────────────────────┐
       │                      │        │                      │
       │     Order Service    │        │     Order Client     │
       │                      │        │                      │
       │     Eureka Client    │        │     Eureka Client    │
       │      Port: 8081      │        │      Port: 8082      │
       │                      │        │                      │
       └──────────────────────┘        └──────────┬───────────┘
                                                 │
                                         Service Discovery
                                                 │
                                                 ▼
                                      ┌──────────────────────┐
                                      │                      │
                                      │     Order Service    │
                                      │                      │
                                      │    GET /api/orders   │
                                      │                      │
                                      └──────────────────────┘
```

The applications have separate responsibilities:

| Application      | Responsibility                                  | Default Port |
|------------------|-------------------------------------------------|-------------:|
| Discovery Server | Maintains the service registry                  |         8761 |
| Order Service    | Registers itself and provides order information |         8081 |
| Order Client     | Discovers Order Service and invokes its API     |         8082 |

The ports shown above are the normal application defaults. The integration test overrides them with dynamically allocated ports.

---

## Discovery Server Configuration

The Discovery Server runs on port `8761` and provides the Eureka dashboard and registry endpoints.

Its `application.yml` configuration is:

```yaml
server:
  port: 8761

spring:
  application:
    name: discovery-server

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

Two important properties are configured.

### `register-with-eureka`

```yaml
register-with-eureka: false
```

This tells the Discovery Server not to register itself as a client with another Eureka Server.

### `fetch-registry`

```yaml
fetch-registry: false
```

This prevents the Discovery Server from fetching a registry from another Eureka Server.

These settings are appropriate for a standalone Eureka Server in this example.

The Discovery Server is responsible for maintaining the registry rather than registering itself as an ordinary application instance.

---

## Order Service Configuration

Order Service is a Spring Boot application that exposes an HTTP endpoint for retrieving order information.

It registers itself with the Discovery Server so that other applications can discover it.

Its `application.yml` configuration is:

```yaml
server:
  port: 8081

spring:
  application:
    name: order-service

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
```

### Application Name

```yaml
spring:
  application:
    name: order-service
```

The application name identifies the service in the Eureka registry.

The Order Client uses this name when looking up the service.

The name is important because service discovery is based on logical service identifiers rather than hardcoded instance addresses.

### Eureka Server URL

```yaml
serviceUrl:
  defaultZone: http://localhost:8761/eureka/
```

This specifies the Eureka Server endpoint to which Order Service connects.

### Service Registration

```yaml
register-with-eureka: true
```

This enables Order Service to register itself with the Eureka Server.

Once registered, the service becomes discoverable by other Eureka clients.

### Registry Fetching

```yaml
fetch-registry: true
```

This allows Order Service to fetch the registry from Eureka.

Although this example primarily demonstrates registration from Order Service, Eureka clients can also retrieve information about other registered services.

### IP Address Preference

```yaml
prefer-ip-address: true
```

This tells Eureka to prefer the IP address when advertising the service instance rather than relying on its hostname.

### Eureka HTTP Transport

```yaml
jersey:
  enabled: false
```

This disables the Jersey transport for Eureka Client.

The example uses Spring Boot's `RestClient` support for Eureka HTTP communication.

This configuration is particularly relevant to the integration-test setup, where the Discovery Server and the Eureka client applications share a classpath.

The Eureka Server dependency brings in Jersey transport classes. Disabling Jersey for the client applications ensures that the configured alternative HTTP transport is selected.

---

## Order Client Configuration

Order Client is another independent Spring Boot application.

It registers itself with Eureka and retrieves the service registry so that it can discover Order Service.

Its `application.yml` configuration is:

```yaml
server:
  port: 8082

spring:
  application:
    name: order-client

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
```

The configuration follows the same principles as Order Service.

The main difference is the application name:

```yaml
spring:
  application:
    name: order-client
```

This identifies Order Client as a separate application in the Eureka registry.

Both applications use the same Discovery Server, allowing them to discover and communicate with one another through the registry.

---

## Service Registration Flow

Service registration is the process through which an application makes its network location known to the Eureka Server.

When Order Service starts, Spring Cloud initializes its Eureka client infrastructure.

The application then registers its instance with the configured Eureka Server.

Conceptually:

```text
Order Service Starts
        |
        ▼
Spring Boot Application Context
        |
        ▼
Eureka Client Initialization
        |
        ▼
Service Registration
        |
        ▼
Eureka Server
        |
        ▼
Order Service Registered
```

The registration contains information that allows other applications to locate the service.

For example:

```text
Application: ORDER-SERVICE
Host:        192.168.1.10
Port:        8081
Status:      UP
```

The actual address and port depend on the environment in which the application runs.

Eureka also maintains service instance availability through its registration and heartbeat mechanisms.

This allows the registry to maintain information about instances that are currently available.

---

## Service Discovery

Service discovery allows a client application to retrieve information about available instances of another service.

In this example, Order Client uses Spring Cloud's `DiscoveryClient`.

The discovery client provides an abstraction for retrieving service instances without requiring application code to communicate directly with Eureka's internal APIs.

### Discovering Order Service

The Order Client uses the logical service name:

```java
private static final String ORDER_SERVICE = "order-service";
```

It then retrieves the available instances:

```java
List<ServiceInstance> instances = discoveryClient.getInstances(ORDER_SERVICE);
```

The `getInstances()` method returns the available instances associated with the requested service identifier.

For example:

```text
Service Registry
       |
       ▼
ORDER-SERVICE
       |
       +-- Instance 1: 192.168.1.10:8081
       |
       +-- Instance 2: 192.168.1.11:8081
       |
       +-- Instance 3: 192.168.1.12:8081
```

If multiple instances are registered, the discovery client can return multiple service instances.

### Handling an Unavailable Service

A service might not be registered or might temporarily be unavailable.

The Order Client checks whether any instances were discovered before attempting communication.

Conceptually:

```java
List<ServiceInstance> instances =
        discoveryClient.getInstances(ORDER_SERVICE);

if (instances.isEmpty()) {
    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Order Service is unavailable");
}
```

This prevents the client from attempting to communicate with a service when no instance has been discovered.

---

## Communicating with a Discovered Service

After discovering an Order Service instance, Order Client uses its network information to construct an HTTP request.

The example uses Spring's `RestClient`.

A simplified implementation looks like:

```java
ServiceInstance instance = instances.getFirst();

String url = instance.getUri() + "/api/orders/" + orderId;

OrderResponse response = restClient.get().uri(url).retrieve().body(OrderResponse.class);
```

The important detail is that the request destination comes from the discovered `ServiceInstance`.

The client does not need to hardcode the Order Service host or port.

The communication flow is:

```text
Order Client
     |
     ▼
DiscoveryClient
     |
     ▼
Eureka Registry
     |
     ▼
Order Service Instance
     |
     ▼
ServiceInstance
     |
     ▼
RestClient
     |
     ▼
HTTP Request
     |
     ▼
Order Service
     |
     ▼
Order Response
```

This separates service location from service communication.

`DiscoveryClient` is responsible for locating the service, while `RestClient` is responsible for sending the HTTP request.

---

## Order Service Endpoint

Order Service exposes an endpoint for retrieving an order:

```http
GET /api/orders/{orderId}
```

For example:

```http
GET /api/orders/ORDER-001
```

The endpoint returns an `OrderResponse`.

A simplified response looks like:

```json
{
  "orderId": "ORDER-001",
  "status": "ACCEPTED",
  "service": "order-service"
}
```

The response demonstrates that the request successfully reached Order Service.

The `service` field identifies the application that handled the request.

---

## Complete Service Discovery Flow

The complete interaction between the three applications follows this sequence:

```text
1. Discovery Server Starts
          |
          ▼
   Eureka Registry Ready
          |
          |
2. Order Service Starts
          |
          ▼
   Initializes Eureka Client
          |
          ▼
   Registers with Eureka
          |
          ▼
   Order Service Available
          |
          |
3. Order Client Starts
          |
          ▼
   Initializes Eureka Client
          |
          ▼
   Fetches Service Registry
          |
          |
4. Order Client Receives Request
          |
          ▼
   GET /api/orders/ORDER-001
          |
          ▼
   DiscoveryClient
          |
          ▼
   Finds ORDER-SERVICE
          |
          ▼
   Retrieves Service Instance
          |
          ▼
   RestClient Sends HTTP Request
          |
          ▼
   Order Service Processes Request
          |
          ▼
   Returns OrderResponse
```

The key idea is that Order Client discovers the service using its logical application name instead of relying on a predefined network address.

---

## Why Use `DiscoveryClient`?

`DiscoveryClient` provides a common abstraction for service discovery.

Instead of coupling application code directly to Eureka-specific APIs, the application uses Spring Cloud's discovery abstraction.

For example:

```java
List<ServiceInstance> instances = discoveryClient.getInstances("order-service");
```

This provides several advantages:

- Application code is less dependent on a specific service registry implementation.
- Service instances can be retrieved dynamically.
- Applications can use logical service names instead of hardcoded addresses.
- Discovery implementations can be changed with less impact on application code.

Spring Cloud provides the discovery abstraction, while Eureka supplies the underlying service registry implementation.

---

## Service Discovery vs Load Balancing

Service discovery and load balancing are related but different concepts.

### Service Discovery

Service discovery determines which instances of a service are available.

```text
Eureka Registry
      |
      ▼
ORDER-SERVICE
      |
      +-- Instance 1
      |
      +-- Instance 2
      |
      +-- Instance 3
```

It provides the information needed to locate service instances.

### Load Balancing

Load balancing determines which available instance should receive a request.

For example:

```text
                 Order Client
                      |
                      ▼
                Load Balancer
                      |
             +--------+--------+
             |        |        |
             ▼        ▼        ▼
          Instance Instance Instance
             1        2        3
```

A load balancer can distribute requests across multiple instances according to a selection strategy.

### What This Example Implements

This example demonstrates service discovery.

The Order Client retrieves the available instances and selects the first one:

```java
ServiceInstance instance = instances.getFirst();
```

It then sends the request to that instance.

This is intentionally simple and makes the discovery process explicit.

It does not implement load balancing across multiple instances.

Load balancing can be explored separately.

---

## Why Use Dynamic Ports in Integration Tests?

The integration test uses:

```java
"--server.port=0"
```

This allows Spring Boot to allocate an available port automatically.

Dynamic ports provide several benefits:

- Avoid port conflicts with other applications.
- Avoid relying on fixed ports during automated tests.
- Allow multiple application contexts to run in the same test environment.
- Make the test more suitable for continuous integration environments.

The actual port is retrieved from the running application context:

```java
private static int portOf(ConfigurableApplicationContext context) {

    return Objects.requireNonNull(((WebServerApplicationContext) context).getWebServer()).getPort();
}
```

The test uses the returned port when constructing HTTP requests and Eureka Server URLs.

---

## Running the Example

The example is organized into separate Maven modules:

```text
service-discovery/
├── discovery-server/
├── order-service/
├── order-client/
└── integration-tests/
```

Start each application independently from the repository root.

### Start Discovery Server

```bash
mvn -pl 09-spring-cloud/service-discovery/discovery-server spring-boot:run
```

The Discovery Server starts on:

```text
http://localhost:8761
```

The Eureka dashboard can be accessed at:

```text
http://localhost:8761
```

### Start Order Service

In another terminal:

```bash
mvn -pl 09-spring-cloud/service-discovery/order-service spring-boot:run
```

Order Service starts on port `8081` and registers itself with Eureka.

### Start Order Client

In another terminal:

```bash
mvn -pl 09-spring-cloud/service-discovery/order-client spring-boot:run
```

Order Client starts on port `8082` and connects to the same Eureka Server.

### Invoke the Order Client

Send a request to:

```http
GET http://localhost:8082/api/orders/ORDER-001
```

A successful response looks like:

```json
{
  "orderId": "ORDER-001",
  "status": "ACCEPTED",
  "service": "order-service"
}
```

---

## Running the Integration Tests

The integration test starts the Discovery Server, Order Service, and Order Client automatically.

Run the integration-test module and its required dependencies from the repository root:

```bash
mvn -pl 09-spring-cloud/service-discovery/integration-tests \
    -am clean verify
```

The `-pl` option selects the integration-test module.

The `-am` option builds the required reactor modules as well.

The `clean verify` lifecycle compiles the applications, executes the tests, and performs the configured verification steps.

---

## Important Implementation Details

### Applications Have Independent Configurations

Each application has its own:

- Spring Boot application class.
- Maven module.
- Application name.
- HTTP port.
- Eureka client configuration.

This reflects how services are commonly deployed in distributed environments.

Although the applications are part of the same Maven project, they are independent Spring Boot applications.

### Discovery Is Based on Logical Names

Order Client discovers Order Service using:

```java
discoveryClient.getInstances("order-service");
```

It does not need to know the actual host or port of Order Service in advance.

The registry provides the instance information.

### Discovery and HTTP Communication Are Separate

The example uses two different responsibilities:

| Component         | Responsibility                                 |
|-------------------|------------------------------------------------|
| `DiscoveryClient` | Locates registered service instances           |
| `ServiceInstance` | Provides the discovered instance information   |
| `RestClient`      | Sends HTTP requests to the discovered instance |

This separation keeps service discovery independent from the HTTP communication mechanism.

### The Example Does Not Implement Load Balancing

The Order Client selects the first discovered instance:

```java
ServiceInstance instance = instances.getFirst();
```

This is sufficient to demonstrate dynamic service discovery.

It should not be interpreted as a load-balancing strategy.

### Eureka Registration Is Not Instantaneous

Eureka clients register and retrieve registry information asynchronously.

Applications should not assume that a service will be discoverable immediately after its startup completes.

The integration test accounts for this behavior by waiting until Order Service appears in Order Client's registry.

---

## Service Discovery vs Static Configuration

The difference between static service configuration and service discovery can be summarized as follows:

| Static Configuration                                 | Service Discovery                                                   |
|------------------------------------------------------|---------------------------------------------------------------------|
| Service addresses are configured manually.           | Service instances register dynamically.                             |
| Clients maintain network locations.                  | Clients discover instances through a registry.                      |
| Address changes may require configuration updates.   | Updated instance information can be published through the registry. |
| Scaling can require manual address management.       | Multiple instances can register under the same service name.        |
| Client code can become coupled to network locations. | Client code can use logical service names.                          |

Service discovery reduces the need for manually maintaining service addresses and makes distributed applications easier to manage.

---

## Key Takeaways

- Service discovery allows applications to locate services dynamically.
- A service registry maintains information about available service instances.
- Spring Cloud Netflix Eureka provides service registration and discovery capabilities.
- Eureka Server acts as the service registry.
- Eureka Clients register themselves and can retrieve information about other services.
- `@EnableEurekaServer` enables a Spring Boot application to act as a Eureka Server.
- `DiscoveryClient` provides an abstraction for retrieving registered service instances.
- `ServiceInstance` contains the information needed to communicate with a discovered service.
- Spring's `RestClient` can be used to communicate with dynamically discovered service instances.
- Logical service names eliminate the need to hardcode service addresses in clients.
- Service discovery and load balancing are related but separate responsibilities.
- Eureka registration and registry fetching are asynchronous operations.
- Integration tests can verify service registration, discovery, and actual application-to-application communication.
- Dynamic ports make integration tests more flexible and reduce port conflicts.
- Independent Spring Boot applications can be tested together using separate application contexts.

---

## Next Example

The next example will explore **Configuration Management** in Spring Cloud, including centralized application configuration, Spring Cloud Config Server, Config Clients, externalized configuration, and managing configuration across multiple distributed applications.
 