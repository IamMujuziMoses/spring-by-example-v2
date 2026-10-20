# Spring Boot Test

This example demonstrates **integration testing with `@SpringBootTest`** in Spring Boot.

It focuses on how Spring Boot initializes an application context for testing, how application configuration and component scanning are applied, and how Spring-managed beans can be injected and tested within the application context.

## What This Example Demonstrates

- What `@SpringBootTest` does.
- Loading a Spring Boot application context for integration testing.
- Using `@SpringBootApplication` as the application's configuration source.
- Component scanning and Spring-managed beans.
- Dependency injection into test classes.
- Verifying application context initialization.
- Testing the behavior of an injected service.
- The difference between `@SpringBootTest` and `@SpringJUnitConfig`.
- Running Spring Boot integration tests with Maven.

---

## What Is `@SpringBootTest`?

`@SpringBootTest` is an annotation provided by Spring Boot's testing support. It allows tests to bootstrap an application context using Spring Boot's application configuration and startup mechanisms.

Unlike a unit test that creates an object directly, an integration test using `@SpringBootTest` can verify that Spring's configuration, component scanning, and dependency injection work together.

A simplified flow looks like:

```text
JUnit Test
    │
    ▼
@SpringBootTest
    │
    ▼
Spring Boot Application
    │
    ▼
Application Context
    │
    ▼
Component Scanning
    │
    ▼
Spring-Managed Beans
    │
    ▼
Test Execution
```

This allows tests to exercise application components within a real Spring application context.

---

## Spring Boot Application Configuration

The example uses a standard Spring Boot application class as the entry point.

### `SpringBootTestApplication.java`

```java
@SpringBootApplication
public class SpringBootTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootTestApplication.class, args);
    }
}
```

The `@SpringBootApplication` annotation combines three important capabilities:

- `@SpringBootConfiguration` identifies the class as a source of Spring Boot configuration.
- `@EnableAutoConfiguration` enables Spring Boot's auto-configuration mechanism.
- `@ComponentScan` enables component scanning from the package containing the application class and its subpackages.

The application class provides the configuration source Spring Boot uses to initialize the application context.

The `main` method starts the application during normal execution. The test does not need to call this method directly because `@SpringBootTest` manages application context initialization.

---

## Service Implementation

The example uses a simple service to demonstrate component scanning and dependency injection.

### `GreetingService.java`

```java
@Service
public class GreetingService {

    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
```

The `@Service` annotation marks `GreetingService` as a Spring-managed component.

Because the service is located in the same package as the application class, Spring Boot's component scanning can discover it automatically.

Conceptually:

```text
SpringBootTestApplication
          │
          ▼
   Component Scanning
          │
          ▼
    GreetingService
          │
          ▼
 Spring Application Context
```

The service does not need to be declared explicitly using an `@Bean` method.

---

## Using `@SpringBootTest`

The test class uses `@SpringBootTest` to load the application context.

```java
@SpringBootTest
public class GreetingServiceTest {
}
```

When Spring Boot discovers the test, it searches for an appropriate `@SpringBootConfiguration`, typically the application's `@SpringBootApplication` class, and uses it to bootstrap the test context.

In this example, the application class is in the same package hierarchy as the test, so Spring Boot can discover it.

By default, `@SpringBootTest` does not start a real web server. The default web environment is `MOCK`, which configures a mock web environment when the required web infrastructure is available.

---

## How `@SpringBootTest` Works

A simplified execution flow is:

```text
JUnit Discovers Test
         │
         ▼
@SpringBootTest
         │
         ▼
Spring Boot Finds Configuration
         │
         ▼
Application Context Initialized
         │
         ▼
Components Discovered
         │
         ▼
Dependencies Injected
         │
         ▼
Test Methods Executed
         │
         ▼
Assertions Verified
```

More specifically:

1. JUnit discovers `GreetingServiceTest`.
2. Spring's testing integration prepares the test context.
3. Spring Boot identifies the application's configuration.
4. Spring Boot initializes the application context.
5. Component scanning discovers `GreetingService`.
6. Spring injects the required objects into the test instance.
7. JUnit executes the test methods.
8. Assertions verify context initialization, component registration, and service behavior.

Spring's TestContext Framework can cache compatible application contexts between tests. This reduces the cost of repeatedly initializing the same context.

---

## `@SpringBootTest` vs. `@SpringJUnitConfig`

Both annotations support Spring integration testing, but they serve different configuration needs.

### `@SpringJUnitConfig`

```java
@SpringJUnitConfig(classes = AppConfig.class)
public class GreetingServiceTest {
}
```

`@SpringJUnitConfig` combines `@ExtendWith(SpringExtension.class)` and `@ContextConfiguration`.

It is useful when a test should load explicitly specified Spring configuration, such as a Java `@Configuration` class.

### `@SpringBootTest`

```java
@SpringBootTest
public class GreetingServiceTest {
}
```

`@SpringBootTest` uses Spring Boot's application bootstrapping mechanisms to create the test application context.

It is useful when testing application configuration, component scanning, auto-configuration, and interactions between Spring-managed components.

The distinction can be summarized as:

| Annotation           | Configuration Approach                | Typical Use                               |
|----------------------|---------------------------------------|-------------------------------------------|
| `@SpringJUnitConfig` | Explicit Spring test configuration    | Focused Spring integration tests          |
| `@SpringBootTest`    | Spring Boot application bootstrapping | Spring Boot application integration tests |

Choose the annotation based on the configuration and application behavior the test needs to exercise.

---

## Web Environment Options

`@SpringBootTest` supports different web environment configurations through its `webEnvironment` attribute.

### `MOCK`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
```

Loads an application context with a mock web environment when web infrastructure is available. It does not start a real embedded web server.

This is the default setting.

### `RANDOM_PORT`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
```

Starts a real web server on a randomly assigned port.

This is useful for integration tests that make actual HTTP requests to the running application.

### `DEFINED_PORT`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
```

Starts a real web server using the configured port, typically from `server.port`.

### `NONE`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
```

Loads a non-web application context.

The current example does not need to specify a web environment because it tests a simple service and uses the default configuration.

---

## Unit Testing vs. `@SpringBootTest`

These approaches test different aspects of an application.

### Unit Test

```java
@Test
void shouldReturnGreeting() {
    GreetingService greetingService = new GreetingService();

    assertEquals("Hello, Spring!", greetingService.greet("Spring"));
}
```

A unit test creates the service directly without starting a Spring application context.

It is suitable for testing the service's behavior in isolation.

### Spring Boot Integration Test

```java
@SpringBootTest
public class GreetingServiceTest {

    @Autowired
    private GreetingService greetingService;
}
```

An integration test obtains the service from the Spring application context.

It can verify that the application configuration, component scanning, and dependency injection work together.

Neither approach replaces the other. Unit tests are generally more focused, while integration tests verify additional framework and application wiring.

---

## Running the Example

From the repository root, run:

```bash
mvn -pl 05-spring-testing/spring-boot-test -am clean verify
```

The `-pl` option selects the example module, while `-am` also builds required reactor modules.

To run only the module's tests, assuming its required dependencies are already available in the local Maven repository:

```bash
mvn -pl 05-spring-testing/spring-boot-test test
```

---

## Expected Results

The example contains three tests:

| Test                            | Expected Result                                      |
|---------------------------------|------------------------------------------------------|
| `shouldLoadApplicationContext`  | The application context is initialized and injected. |
| `shouldRegisterGreetingService` | The service is registered and injected by Spring.    |
| `shouldReturnGreeting`          | The service returns `Hello, Spring!`.                |

All three tests should pass when the application is configured correctly.

---

## Important Testing Detail

`@SpringBootTest` creates an application context, so it is more comprehensive than a unit test that instantiates a class directly.

However, loading the application context does not automatically mean that every application feature has been tested.

For example, this service test does not verify:

- HTTP request handling.
- Database connectivity.
- Security filter chains.
- External service integrations.
- Application behavior under production deployment conditions.

Those concerns require additional tests and, where appropriate, specialized Spring testing support.

---

## Key Takeaways

- `@SpringBootTest` bootstraps a Spring Boot application context for integration testing.
- Spring Boot can discover the application's configuration through `@SpringBootApplication`.
- Component scanning discovers the `GreetingService` component.
- `@Autowired` injects Spring-managed objects into the test.
- Context-loading tests help identify configuration and dependency injection problems.
- The default web environment does not start a real web server.
- `@SpringBootTest` and `@SpringJUnitConfig` support different integration testing needs.
- Unit tests and integration tests complement each other.

---

## What This Example Does Not Cover

This example intentionally focuses on application context integration testing.

It does not cover:

- MVC testing with `MockMvc`.
- Test slices such as `@WebMvcTest`.
- Database integration testing.
- Transaction testing.
- Security testing.
- Testcontainers.
- Running tests against a real HTTP server.
- Application context caching in detail.

These topics are explored in separate examples throughout Module 05 — Spring Testing.

---

## Next Example

The next example will explore **Spring MVC testing**, including how to test controller behavior and HTTP request handling.
