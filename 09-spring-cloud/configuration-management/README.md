# Configuration Management

This example demonstrates **centralized configuration management** using Spring Cloud Config.

It focuses on how Spring Cloud Config Server provides externalized configuration to Spring Boot applications, how configuration is stored and organized in a Git repository, and how Spring Cloud Config Client retrieves configuration during application startup.

The example uses an Order Service to demonstrate how application-specific, shared, and profile-specific configuration can be maintained independently of application code.

## What This Example Demonstrates

- What centralized configuration management means.
- Spring Cloud Config Server.
- Spring Cloud Config Client.
- Git-backed configuration repositories.
- Externalized application configuration.
- Shared configuration across applications.
- Application-specific configuration.
- Profile-specific configuration.
- Spring Boot Config Data integration using `spring.config.import`.
- Binding external configuration using `@ConfigurationProperties`.
- Integration testing of Config Server and Config Client.

---

## What Is Configuration Management?

Configuration management is the practice of managing application settings independently of application code.

Applications commonly require configuration values such as:

- Database connection details.
- Service endpoints.
- Feature flags.
- Application-specific settings.
- Environment-specific values.
- Integration credentials.

Without centralized configuration management, each application may maintain its own configuration files. This can make configuration difficult to maintain across multiple services and environments.

Spring Cloud Config provides a centralized approach.

Instead of maintaining configuration separately in every application, configuration can be stored in a central repository and served to applications through a Config Server.

A simplified architecture looks like:

```text
                 Git Repository
                       │
                       ▼
              Spring Cloud Config
                     Server
                       │
                       ▼
                Spring Cloud
                  Config Client
                       │
                       ▼
                  Order Service
                       │
                       ▼
             Application Properties
```

This allows applications to retrieve their configuration from a centralized source.

---

## Spring Cloud Config

Spring Cloud Config provides server-side and client-side support for externalized configuration in distributed systems.

It consists of two main components:

### Config Server

The Config Server exposes configuration through an HTTP API.

It retrieves configuration from a backing repository, such as Git, and makes it available to registered clients.

The Config Server can provide:

- Shared application configuration.
- Application-specific configuration.
- Profile-specific configuration.
- Configuration from different Git branches or labels.

### Config Client

The Config Client connects to a Config Server and retrieves configuration for its application.

It integrates with Spring Boot's externalized configuration mechanism, allowing remotely retrieved properties to participate in the application's Environment.

The client identifies the configuration it needs using its application name and active profiles.

---

### Modules

| Module                 | Responsibility                                                                      |
|------------------------|-------------------------------------------------------------------------------------|
| `cm-config-server`     | Hosts the centralized configuration server.                                         |
| `cm-order-service`     | Retrieves external configuration and exposes an endpoint showing the loaded values. |
| `cm-integration-tests` | Starts both applications and verifies centralized configuration retrieval.          |
| `config-repo`          | Stores shared, application-specific, and profile-specific configuration files.      |

---

## Config Server

The Config Server is a Spring Boot application enabled with `@EnableConfigServer`.

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

The `@EnableConfigServer` annotation enables Spring Cloud Config Server functionality.

It allows the application to expose configuration through HTTP endpoints.

### Config Server Dependencies

The Config Server module uses:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

### Config Server Configuration

The Config Server is configured in `application.yml`:

```yaml
server:
  port: 8888

spring:
  application:
    name: cm-config-server
  cloud:
    config:
      server:
        git:
          uri: ${CONFIG_REPO_URI}
          default-label: main
```

The configuration properties have the following purposes:

| Property                                       | Description                                                  |
|------------------------------------------------|--------------------------------------------------------------|
| `server.port`                                  | Defines the Config Server's HTTP port.                       |
| `spring.application.name`                      | Identifies the Config Server application.                    |
| `spring.cloud.config.server.git.uri`           | Specifies the Git repository containing configuration files. |
| `spring.cloud.config.server.git.default-label` | Specifies the default Git branch or label.                   |

The Git repository URI is supplied through the `CONFIG_REPO_URI` environment variable.

This keeps the repository location outside the application's source configuration.

The default label is `main`, so the Config Server retrieves configuration from that branch unless another label is requested.

---

## Git-Backed Configuration Repository

Spring Cloud Config supports Git as a configuration backend.

The repository contains YAML files that define configuration at different levels.

This example uses three configuration files:

```text
config-repo/
├── application.yml
├── cm-order-service.yml
└── cm-order-service-dev.yml
```

### Shared Configuration

The `application.yml` file contains configuration shared across applications.

```yaml
app:
  order:
    currency: USD
```

The `application.yml` file is not specific to the Order Service.

It provides common configuration that can be inherited by applications retrieving configuration from the repository.

### Application-Specific Configuration

The `cm-order-service.yml` file contains configuration specific to the Order Service.

```yaml
app:
  order:
    message: Orders are processed using centralized configuration.
    max-items: 10
    expedited-enabled: false
```

The filename corresponds to the application's name:

```yaml
spring:
  application:
    name: cm-order-service
```

Spring Cloud Config uses the application name to locate the corresponding configuration file.

### Profile-Specific Configuration

The `cm-order-service-dev.yml` file contains configuration specific to the `dev` profile.

```yaml
app:
  order:
    message: Orders are processed using the development configuration.
    max-items: 25
    expedited-enabled: true
```

This configuration is applied when the Order Service runs with the `dev` profile.

Profile-specific configuration allows different environments to use different settings without changing application code.

For example:

| Property            | Default configuration                                 | Development configuration                                 |
|---------------------|-------------------------------------------------------|-----------------------------------------------------------|
| `message`           | Orders are processed using centralized configuration. | Orders are processed using the development configuration. |
| `max-items`         | 10                                                    | 25                                                        |
| `currency`          | USD                                                   | USD                                                       |
| `expedited-enabled` | false                                                 | true                                                      |

The `currency` property remains unchanged because it is inherited from the shared `application.yml` configuration.

---

## Configuration File Naming Convention

Spring Cloud Config uses a naming convention to locate configuration files.

The general structure is:

```text
{application}.yml
{application}-{profile}.yml
```

For example:

```text
application.yml
cm-order-service.yml
cm-order-service-dev.yml
cm-order-service-prod.yml
```

The files have different responsibilities:

| File                        | Purpose                                                   |
|-----------------------------|-----------------------------------------------------------|
| `application.yml`           | Shared configuration for applications.                    |
| `cm-order-service.yml`      | Configuration specific to the Order Service.              |
| `cm-order-service-dev.yml`  | Development-specific configuration for the Order Service. |
| `cm-order-service-prod.yml` | Production-specific configuration for the Order Service.  |

This convention allows Spring Cloud Config to resolve configuration based on the requesting application's name and active profiles.

---

## Config Client

The Order Service acts as a Spring Cloud Config Client.

It retrieves its configuration from the Config Server during application startup.

### Config Client Dependencies

The Order Service uses Spring Boot's web starter and Spring Cloud Config Client.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

The `spring-cloud-starter-config` dependency provides the Config Client integration.

### Config Client Configuration

The Order Service declares its application name and Config Server location in `application.yml`.

```yaml
server:
  port: 8081

spring:
  application:
    name: cm-order-service
  config:
    import: configserver:http://localhost:8888
```

The important property is:

```yaml
spring:
  config:
    import: configserver:http://localhost:8888
```

The `configserver:` prefix tells Spring Boot to retrieve additional configuration through Spring Cloud Config.

This uses Spring Boot's Config Data mechanism.

The import is deliberately non-optional.

If the Config Server is unavailable, the application fails to start rather than silently continuing without its centralized configuration.

For applications where starting without remote configuration is acceptable, an optional import can be used:

```yaml
spring:
  config:
    import: optional:configserver:http://localhost:8888
```

The choice depends on whether remote configuration is mandatory for the application to operate correctly.

---

## Spring Boot Config Data Integration

Modern Spring Cloud Config Client integrates with Spring Boot through the Config Data API.

The `spring.config.import` property is the recommended way to declare a Config Server dependency.

During application startup, Spring Boot processes the configuration import and Spring Cloud Config Client retrieves the required configuration.

Conceptually:

```text
Order Service Startup
        │
        ▼
Spring Boot Config Data
        │
        ▼
Config Server Import
        │
        ▼
Config Client
        │
        ▼
HTTP Request to Config Server
        │
        ▼
Configuration Retrieved
        │
        ▼
Spring Environment
        │
        ▼
Application Startup Continues
```

The retrieved properties become part of the Spring Environment and can be consumed through Spring's normal configuration mechanisms.

This example uses `spring.config.import` rather than the older `bootstrap.yml` configuration approach.

---

## How Config Server Resolves Configuration

The Config Client requests configuration using the application name, active profile, and optionally a Git label.

The general Config Server endpoint format is:

```text
/{application}/{profile}/{label}
```

For example:

```text
/cm-order-service/dev/main
```

The endpoint identifies:

| Parameter   | Value              | Description                            |
|-------------|--------------------|----------------------------------------|
| Application | `cm-order-service` | The requesting application's name.     |
| Profile     | `dev`              | The active environment profile.        |
| Label       | `main`             | The Git branch or configuration label. |

The Config Server uses these values to locate and combine the relevant configuration sources.

The application can therefore retrieve different configuration without changing its Java code.

---

## Configuration Resolution and Property Precedence

The example combines shared, application-specific, and profile-specific configuration.

For the development profile, the relevant configuration sources are:

```text
application.yml
       │
       ▼
cm-order-service.yml
       │
       ▼
cm-order-service-dev.yml
```

The shared configuration provides common values.

The application-specific configuration provides Order Service defaults.

The profile-specific configuration supplies development overrides.

When multiple sources define the same property, Spring's property-source precedence determines which value is effective.

For this example:

| Property                      | Effective value                                           | Source                     |
|-------------------------------|-----------------------------------------------------------|----------------------------|
| `app.order.message`           | Orders are processed using the development configuration. | `cm-order-service-dev.yml` |
| `app.order.max-items`         | 25                                                        | `cm-order-service-dev.yml` |
| `app.order.currency`          | USD                                                       | `application.yml`          |
| `app.order.expedited-enabled` | true                                                      | `cm-order-service-dev.yml` |

This demonstrates how shared configuration and environment-specific overrides work together.

---

## Binding External Configuration

The Order Service uses `@ConfigurationProperties` to bind external configuration into a strongly typed Java object.

### OrderProperties

```java
@ConfigurationProperties(prefix = "app.order")
public record OrderProperties(String message, int maxItems, String currency, boolean expeditedEnabled) {}
```

The `@ConfigurationProperties` annotation tells Spring Boot to bind properties beginning with `app.order` to the record components.

For example:

```yaml
app:
  order:
    message: Orders are processed using centralized configuration.
    max-items: 10
    currency: USD
    expedited-enabled: false
```

The YAML properties are mapped to the corresponding Java fields.

Spring Boot supports relaxed binding, so kebab-case property names such as `max-items` map to Java record components such as `maxItems`.

### Configuration Properties Scanning

The Order Service enables configuration properties scanning:

```java
@SpringBootApplication
@ConfigurationPropertiesScan
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

The `@ConfigurationPropertiesScan` annotation discovers classes annotated with `@ConfigurationProperties` and registers them for binding.

This keeps configuration binding separate from application business logic.

---

## Exposing the Loaded Configuration

The Order Service exposes an endpoint that returns the configuration values loaded from the Config Server.

### Response Model

```java
public record OrderConfigurationResponse(String message, int maxItems, String currency, boolean expeditedEnabled) {}
```

The response model represents the configuration values exposed by the endpoint.

### Controller

```java
@RestController
@RequestMapping("/api/orders")
public class OrderConfigurationController {

    private final OrderProperties orderProperties;

    public OrderConfigurationController(OrderProperties orderProperties) {
        this.orderProperties = orderProperties;
    }

    @GetMapping("/configuration")
    public OrderConfigurationResponse configuration() {
        return new OrderConfigurationResponse(orderProperties.message(), orderProperties.maxItems(),
                orderProperties.currency(), orderProperties.expeditedEnabled());
    }
}
```

The controller reads the bound configuration properties and returns them as a REST response.

This endpoint makes it possible to verify that the Order Service is using the values retrieved from the Config Server.

### Example Response

When the Order Service runs with the `dev` profile, the endpoint returns:

```json
{
  "message": "Orders are processed using the development configuration.",
  "maxItems": 25,
  "currency": "USD",
  "expeditedEnabled": true
}
```

The response demonstrates that the application has received and applied the expected centralized configuration.

---

## Integration Testing

The example includes an integration test that starts both the Config Server and the Order Service.

The test verifies the interaction between the two applications rather than testing configuration binding in isolation.

The integration test performs the following steps:

1. Creates a temporary Git repository.
2. Copies the example configuration files into the repository.
3. Initializes Git and commits the configuration files.
4. Starts the Config Server on a random HTTP port.
5. Starts the Order Service on another random HTTP port.
6. Activates the `dev` profile.
7. Configures the Order Service to import configuration from the running Config Server.
8. Sends an HTTP request to the Order Service.
9. Verifies the returned configuration values.
10. Shuts down both applications and removes the temporary repository.

### Temporary Git Repository

The test creates a temporary repository so that it does not depend on an external Git server.

The repository is initialized and committed programmatically.

Conceptually:

```text
Example Configuration Files
           │
           ▼
Temporary Directory
           │
           ▼
Git Repository Initialization
           │
           ▼
Commit Configuration Files
           │
           ▼
Config Server Uses Repository
```

This makes the integration test self-contained and repeatable.

### Starting the Applications

The test starts the Config Server using `SpringApplicationBuilder`.

```java
configServer = new SpringApplicationBuilder(ConfigServerApplication.class)
        .run(
                "--server.port=0",
                "--spring.cloud.config.server.git.uri="
                        + temporaryConfigRepository.toUri()
        );
```

The `server.port=0` argument instructs the embedded server to select an available random port.

The test then retrieves the actual port and uses it when starting the Order Service.

```java
orderService = new SpringApplicationBuilder(OrderServiceApplication.class)
        .run(
                "--server.port=0",
                "--spring.application.name=cm-order-service",
                "--spring.profiles.active=dev",
                "--spring.cloud.config.enabled=true",
                "--spring.config.import=configserver:" + configServerUrl
        );
```

This ensures that the Order Service retrieves configuration from the Config Server instance started by the test.

### Verifying Configuration Retrieval

The test sends an HTTP request to the Order Service:

```java
OrderConfigurationResponse response = RestClient.create()
        .get()
        .uri("http://localhost:" + orderServicePort + "/api/orders/configuration")
        .retrieve()
        .body(OrderConfigurationResponse.class);
```

It then verifies the returned values:

```java
assertThat(response).isNotNull();
assertThat(response.message())
        .isEqualTo("Orders are processed using the development configuration.");
assertThat(response.maxItems()).isEqualTo(25);
assertThat(response.currency()).isEqualTo("USD");
assertThat(response.expeditedEnabled()).isTrue();
```

These assertions verify that:

- The development-specific message was loaded.
- The development-specific maximum item count was applied.
- The shared currency configuration was inherited.
- The development-specific expedited order setting was applied.

### Why Use Random Ports?

The test starts both applications with:

```text
--server.port=0
```

This avoids depending on fixed ports such as `8888` and `8081`.

It also prevents port conflicts when tests run in environments where those ports may already be occupied.

### Application Cleanup

The test closes both application contexts after execution.

It also deletes the temporary Git repository.

This prevents application instances and temporary files from being left behind after the test completes.

---

## Configuration Management Flow

The complete flow demonstrated by this example is:

```text
                 Git Repository
                       │
                       │ Configuration Files
                       ▼
              Spring Cloud Config
                     Server
                       │
                       │ HTTP Configuration Request
                       ▼
              Spring Cloud Config
                     Client
                       │
                       ▼
              Spring Boot Config Data
                       │
                       ▼
                Spring Environment
                       │
                       ▼
             @ConfigurationProperties
                       │
                       ▼
                  Order Service
                       │
                       ▼
              REST Configuration Endpoint
```

The Config Server retrieves configuration from Git and exposes it through HTTP.

The Config Client retrieves the configuration during application startup.

Spring Boot makes the retrieved values available through the Environment, allowing the application to bind and consume them.

---

## Running the Example

The Config Server and Order Service can be started independently.

### Start the Config Server

From the repository root:

```bash
export CONFIG_REPO_URI=/absolute/path/to/config-repo

mvn -pl 09-spring-cloud/configuration-management/cm-config-server spring-boot:run
```

Replace `/absolute/path/to/config-repo` with the absolute path to the example's `config-repo` directory.

The Config Server starts on port `8888`.

### Start the Order Service

In another terminal:

```bash
mvn -pl 09-spring-cloud/configuration-management/cm-order-service spring-boot:run
```

The Order Service connects to the Config Server at:

```text
http://localhost:8888
```

It retrieves configuration using its application name and active profile.

To run with the development profile:

```bash
mvn -pl 09-spring-cloud/configuration-management/cm-order-service spring-boot:run \
    -Dspring-boot.run.profiles=dev
```

### Access the Configuration Endpoint

Once the Order Service has started, access:

```text
http://localhost:8081/api/orders/configuration
```

The endpoint returns the configuration values loaded by the application.

---

## Running the Tests

Run the integration tests directly from the module:

```bash
mvn test
```

Or run the integration test module and its required dependencies from the repository root:

```bash
mvn -pl 09-spring-cloud/configuration-management/cm-integration-tests -am verify
```

The `-am` option builds the required Maven modules in addition to the selected module.

The `verify` lifecycle executes the integration tests and validates the example.

---

## Important Considerations

### Configuration Server Availability

The Order Service uses a non-optional Config Server import:

```yaml
spring:
  config:
    import: configserver:http://localhost:8888
```

This means the Config Server must be available when the Order Service starts.

For production applications, the Config Server should be deployed with appropriate availability, monitoring, and network configuration.

### Configuration Repository Security

Configuration repositories may contain sensitive values such as credentials and API keys.

Sensitive configuration should not be committed as plain text to an ordinary Git repository.

Production deployments should use appropriate secret management and encryption mechanisms.

### Configuration Refresh

This example retrieves configuration during application startup.

It does not demonstrate refreshing configuration in an already running application.

Configuration refresh and runtime configuration updates can be explored separately.

### Local Git Repository

The integration test uses a temporary local Git repository rather than an external Git hosting provider.

This keeps the test self-contained and avoids external network dependencies.

---

## Key Takeaways

- Spring Cloud Config provides centralized configuration management for distributed applications.
- Config Server exposes configuration through HTTP.
- Config Client retrieves configuration from Config Server.
- Git can be used as a configuration repository.
- `application.yml` provides shared configuration.
- Application-specific configuration is resolved using the application name.
- Profile-specific configuration supports environment-specific settings.
- `spring.config.import` integrates Config Client with Spring Boot's Config Data mechanism.
- `@ConfigurationProperties` provides strongly typed configuration binding.
- Spring Cloud Config combines configuration sources according to property-source precedence.
- Integration tests can verify configuration retrieval by starting actual Config Server and Client applications.
- A temporary Git repository makes the integration test independent of an external Git server.

---

## Next Example

The next example will explore **API Gateways** in Spring Cloud, including how a gateway provides a single entry point to distributed services, routes requests, and integrates with service discovery.
