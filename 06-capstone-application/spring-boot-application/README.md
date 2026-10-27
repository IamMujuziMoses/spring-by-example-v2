# Spring Boot Application Setup

This example demonstrates **Spring Boot application setup** in Spring by Example V2.

It focuses on creating a Spring Boot application, initializing the Spring application context, enabling auto-configuration and component scanning, defining application properties, and verifying that the application starts successfully.

The example establishes the foundation for the capstone application, which will progressively integrate Spring Data, REST APIs, Spring Security, transaction management, Spring Modulith, and application testing.

## What This Example Demonstrates

- Creating a Spring Boot application using `@SpringBootApplication`.
- Starting an application with `SpringApplication.run()`.
- Understanding Spring Boot auto-configuration.
- Understanding component scanning and configuration classes.
- Defining application-specific properties using `application.properties`.
- Initializing the Spring application context.
- Testing application startup with `@SpringBootTest`.
- Verifying that the application configuration is registered as a Spring bean.
- Establishing a foundation for integrating other Spring technologies.

---

## What Is Spring Boot Application Setup?

Spring Boot simplifies the process of creating Spring applications by providing conventions, auto-configuration, and dependency management.

A traditional Spring application may require explicit configuration for infrastructure components, application context initialization, and framework integration.

Spring Boot reduces much of this setup by using the application's dependencies and configuration to determine which components should be configured automatically.

A simplified application startup flow looks like:

```text
Application Entry Point
          │
          ▼
SpringApplication.run()
          │
          ▼
Spring Application Context
          │
          ▼
Auto-Configuration
          │
          ▼
Component Scanning
          │
          ▼
Bean Initialization
          │
          ▼
Application Ready
```

The application context is the central container that manages Spring beans and their dependencies.

This example starts with a minimal application so that the startup process can be understood before introducing persistence, web endpoints, and security.

---

## Spring Boot Application Entry Point

The main application class is `SpringBootApplicationSetup`.

```java
@SpringBootApplication
public class SpringBootApplicationSetup {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringBootApplicationSetup.class, args);
    }
}
```

The class serves as both the application's entry point and its primary Spring configuration class.

Two elements are particularly important:

- `@SpringBootApplication`
- `SpringApplication.run()`

### `@SpringBootApplication`

The `@SpringBootApplication` annotation is the standard starting point for most Spring Boot applications.

It combines three annotations:

- `@SpringBootConfiguration` — identifies the class as a source of Spring configuration.
- `@EnableAutoConfiguration` — enables Spring Boot's auto-configuration mechanism.
- `@ComponentScan` — enables scanning for eligible Spring components in the application's package hierarchy.

Conceptually:

```text
@SpringBootApplication
          │
          ├── @SpringBootConfiguration
          │
          ├── @EnableAutoConfiguration
          │
          └── @ComponentScan
```

Together, these annotations establish the configuration source, enable automatic configuration, and allow Spring to discover application components.

### `SpringApplication.run()`

The application's `main()` method invokes:

```java
SpringApplication.run(SpringBootApplicationSetup.class, args);
```

The first argument identifies the primary application configuration class. The second passes command-line arguments to Spring Boot.

This call bootstraps the application, creates and refreshes the application context, and initializes the beans required by the configured application.

When the application starts successfully, Spring Boot completes the startup process and keeps the application running according to its configured runtime behavior.

Because this example uses the basic Spring Boot starter without a web starter, it does not start an embedded HTTP server.

---

## Understanding Spring Boot Auto-Configuration

Auto-configuration allows Spring Boot to configure common application infrastructure based on the dependencies available on the classpath and the application's existing configuration.

For example, adding suitable dependencies can allow Spring Boot to configure infrastructure for:

- Web applications.
- Data access.
- Database connectivity.
- Validation.
- Security.

The actual configuration depends on the dependencies present, the application's configuration, and the conditions associated with individual auto-configuration classes.

The general flow is:

```text
Maven Dependencies
        │
        ▼
Application Configuration
        │
        ▼
Auto-Configuration Conditions
        │
        ▼
Applicable Configuration
        │
        ▼
Spring Beans
```

Auto-configuration does not mean that every Spring feature is enabled automatically. The relevant dependencies and configuration must still be present.

In this example, the basic Spring Boot starter provides the foundation for running the application. Database, web, and security capabilities will be introduced in subsequent examples.

---

## Understanding Component Scanning

Component scanning allows Spring to discover classes that are eligible for registration as beans.

Common component annotations include:

- `@Component`
- `@Service`
- `@Repository`
- `@Controller`
- `@RestController`

Spring also registers beans declared through configuration methods such as `@Bean`.

By default, component scanning begins in the package containing the class annotated with `@SpringBootApplication` and continues into its subpackages.

For this example:

```text
com.springbyexample.v2.springbootapplication
                         │
                         ▼
              SpringBootApplicationSetup
                         │
                         ▼
                 Component Scanning
                         │
             ┌───────────┴───────────┐
             ▼                       ▼
       Current Package          Subpackages
```

This package arrangement makes the application class a natural root for discovering application components.

**Important:** The current example does not define a separate `@Component` or `@Service` class. It establishes the component-scanning configuration; a later example can demonstrate discovery of application services and their dependencies.

---

## Application Properties

Spring Boot provides conventional configuration files for defining application settings.

This example uses `src/main/resources/application.properties`:

```properties
spring.application.name=spring-boot-application-setup
```

The `spring.application.name` property assigns a logical name to the application.

An application name can be useful when identifying an application in logs, monitoring systems, or integrations that use the Spring environment.

Spring Boot loads the standard `application.properties` file from the application's resources and makes its values available through the Spring `Environment` and supported configuration mechanisms.

As the capstone develops, additional properties can be introduced for settings such as:

- Database connection details.
- Server configuration.
- Logging.
- Security.
- External service integration.

Environment-specific configuration and externalized secrets should be handled appropriately when those capabilities are added.

---

## The Spring Application Context

The Spring application context is responsible for managing the application's Spring beans and providing infrastructure for dependency injection.

A bean is an object managed by Spring rather than an object whose entire lifecycle is managed directly by application code.

The context supports capabilities such as:

- Bean creation and lifecycle management.
- Dependency injection.
- Configuration processing.
- Access to the Spring environment.
- Integration between Spring infrastructure components.

A simplified view of this example is:

```text
SpringBootApplicationSetup
             │
             ▼
    Spring Application Context
             │
             ├── Application Configuration
             │
             ├── Environment and Properties
             │
             └── Auto-Configured Infrastructure
```

The exact beans created depend on the application's dependencies and configuration.

As additional modules are introduced, the context will also manage the components needed to connect application services, repositories, controllers, and security infrastructure.

---

## Maven Dependencies

The module uses Spring Boot's basic starter and test starter.

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### `spring-boot-starter`

The basic starter provides core Spring Boot functionality and the dependencies needed for a minimal application.

It is sufficient for demonstrating application startup without introducing an HTTP server or database.

### `spring-boot-starter-test`

The test starter supplies testing support used by Spring Boot applications, including Spring's testing integration and commonly used testing libraries.

The `test` scope ensures that the test dependencies are used for testing rather than being included as normal application dependencies.

The module inherits dependency management from the parent Maven project, so dependency versions should remain consistent with the versions managed by the repository.

---

## Application Startup Flow

The complete startup process demonstrated by this example can be summarized as follows:

```text
Java main()
    │
    ▼
SpringApplication.run()
    │
    ▼
Read Application Configuration
    │
    ▼
Create Spring Application Context
    │
    ▼
Process @SpringBootApplication
    │
    ├── Enable Auto-Configuration
    │
    ├── Configure Component Scanning
    │
    └── Register Configuration
    │
    ▼
Initialize Required Beans
    │
    ▼
Application Context Ready
```

The application does not yet expose business functionality. Its purpose is to establish a working Spring Boot foundation on which that functionality can be built.

---

## Testing vs Running the Application

Testing and running the application exercise related but different paths.

### Running the Application

When the application is started using its `main()` method, Spring Boot initializes the application for normal execution.

```text
main()
  │
  ▼
SpringApplication.run()
  │
  ▼
Application Context
  │
  ▼
Application Running
```

### Running the Tests

When the tests run, `@SpringBootTest` creates or reuses a suitable test context.

```text
JUnit Test
    │
    ▼
@SpringBootTest
    │
    ▼
Spring Test Context
    │
    ▼
Application Configuration
    │
    ▼
Assertions
```

Spring's test context framework can cache compatible application contexts between tests, reducing repeated initialization work.

The example focuses on startup verification rather than testing a business operation or HTTP endpoint.

---

## Running the Example

From the repository root, run the application using Maven:

```bash
mvn -pl 06-capstone-application/spring-boot-application-setup spring-boot:run
```

If the repository includes the Maven Wrapper, use:

```bash
./mvnw -pl 06-capstone-application/spring-boot-application-setup spring-boot:run
```

The application starts without requiring a database or external service.

Because the example does not include a web starter, there is no HTTP endpoint to visit in a browser.

---

## Running the Tests

Run the tests for this module from the repository root:

```bash
mvn -pl 06-capstone-application/spring-boot-application-setup test
```

To run the tests using the Maven Wrapper:

```bash
./mvnw -pl 06-capstone-application/spring-boot-application-setup test
```

To build the module and its required reactor dependencies, use:

```bash
mvn -pl 06-capstone-application/spring-boot-application-setup -am test
```

The `-am` option tells Maven to also build the modules required by the selected module.

---

## Key Takeaways

- `@SpringBootApplication` establishes the primary Spring Boot configuration and enables auto-configuration and component scanning.
- `SpringApplication.run()` bootstraps the application.
- Auto-configuration applies suitable configuration based on classpath dependencies and conditions.
- Component scanning discovers eligible Spring components within the configured package hierarchy.
- `application.properties` provides a conventional location for application settings.
- The Spring application context manages beans and supports dependency injection.
- `@SpringBootTest` allows tests to verify application context initialization.
- Application startup tests establish a foundation for more comprehensive integration testing.
- A minimal Spring Boot application can be created without introducing a web server or database.

---

## What This Example Does Not Cover

This example intentionally focuses on the initial Spring Boot application setup.

It does not cover:

- Defining and injecting application services.
- Connecting the existing project and task domain to Spring-managed beans.
- Spring Data repositories or database persistence.
- REST controllers and HTTP request handling.
- Spring Security configuration.
- Transaction management.
- Spring Modulith module verification.
- Testcontainers or database integration tests.
- End-to-end application testing.
- Production deployment and observability.

These concerns will be introduced incrementally as the capstone application develops.

---

## Next Example

The next example will explore **Spring Data and Persistence**, introducing database-backed repositories and persistence configuration to build on the Spring Boot foundation established here.
