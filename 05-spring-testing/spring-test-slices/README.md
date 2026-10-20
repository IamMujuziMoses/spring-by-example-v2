# Spring Test Slices

This example demonstrates **Spring Test Slices** using Spring Boot's `@WebMvcTest` annotation.

It focuses on how Spring Boot loads a limited application context for testing a specific application layer, how to test MVC controllers independently of their service implementations, and how to use `@MockitoBean` to replace a service dependency with a mock.

## What This Example Demonstrates

- What Spring Test Slices are.
- Why focused test contexts are useful.
- The purpose of `@WebMvcTest`.
- Limiting an MVC test to a specific controller.
- Testing controllers with `MockMvc`.
- Replacing service dependencies with `@MockitoBean`.
- Configuring mock behavior with Mockito.
- Verifying HTTP response status codes and response bodies.
- Verifying interactions between controllers and mocked services.
- Comparing `@WebMvcTest` with `@SpringBootTest`.

---

## What Are Spring Test Slices?

Spring Test Slices are testing approaches that load only the parts of an application needed to test a particular layer or technology.

A typical Spring Boot application may contain controllers, services, repositories, database configuration, security configuration, and other components.

Loading the entire application context is useful for integration testing, but it can be unnecessary when the objective is to test only the MVC layer.

Spring Boot provides test slice annotations that configure a focused test context for specific application layers.

For example, `@WebMvcTest` focuses on Spring MVC components.

A simplified application structure looks like:

```text
Spring Boot Application
          │
          ├── Controller Layer
          │
          ├── Service Layer
          │
          ├── Repository Layer
          │
          └── Database Layer
```

An MVC slice test focuses on the controller and its web-related infrastructure:

```text
@WebMvcTest
     │
     ▼
MVC Test Context
     │
     ├── Controller
     │
     ├── Spring MVC Infrastructure
     │
     └── Mock Service Dependency
```

The test can verify request handling and response behavior without requiring the full application context.

---

## What Is @WebMvcTest?

`@WebMvcTest` is a Spring Boot testing annotation for testing Spring MVC controllers and related web components.

In this example, it is used as follows:

```java
@WebMvcTest(GreetingController.class)
public class GreetingControllerTest {
}
```

The annotation tells Spring Boot to configure an MVC-focused test context for `GreetingController`.

The class argument identifies the controller under test.

This focused approach is useful when testing:

- Request mappings.
- HTTP methods.
- Query parameter binding.
- Request validation.
- Response status codes.
- Response bodies.
- MVC-specific exception handling.

The annotation also auto-configures `MockMvc`, which allows the test to simulate HTTP requests without starting a real web server.

---

## Why Specify the Controller Class?

The example uses:

```java
@WebMvcTest(GreetingController.class)
```

Specifying the controller makes the intended scope of the test explicit.

Instead of testing the entire application, the test focuses on the MVC behavior of `GreetingController`.

This keeps the example focused on one responsibility: verifying how the controller handles HTTP requests and interacts with its dependency.

Other controllers and application layers are not automatically loaded as a full application would load them.

---

## Application Structure

The example contains the following classes:

### SpringTestSlicesApplication

The application entry point uses `@SpringBootApplication` to configure the Spring Boot application.

```java
@SpringBootApplication
public class SpringTestSlicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTestSlicesApplication.class, args);
    }
}
```

### GreetingService

The service provides greeting messages:

```java
@Service
public class GreetingService {

    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
```

The service contains the greeting logic.

In a full application context, Spring would discover this class as a service component and register it as a bean.

In this MVC slice test, the test supplies a mock instead of relying on the real service implementation.

### GreetingController

The controller exposes a GET endpoint:

```java
@RestController
public class GreetingController {

    private final GreetingService greetingService;

    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/greetings")
    public String greet(@RequestParam(defaultValue = "Spring") String name) {
        return greetingService.greet(name);
    }
}
```

The endpoint accepts an optional `name` query parameter.

If the parameter is omitted, Spring MVC uses `"Spring"` as the default value.

The controller passes the resulting name to `GreetingService` and returns the service's response.

---

## Why Is the Service Mocked?

The controller depends on `GreetingService`.

In a full application context, the real service would normally be registered and injected.

However, an MVC slice test is designed to focus on web-layer behavior rather than the implementation of the service layer.

The test therefore replaces the service dependency with a mock.

Conceptually:

```text
HTTP Request
     │
     ▼
GreetingController
     │
     ▼
Mock GreetingService
     │
     ▼
Configured Greeting
     │
     ▼
HTTP Response
```

The mock allows the test to control the service response and verify that the controller passes the correct arguments.

This isolates the controller's request-handling behavior from the real service implementation.

---

## Using @MockitoBean

The test declares the service mock with:

```java
@MockitoBean
private GreetingService greetingService;
```

`@MockitoBean` creates a Mockito mock and registers it in the Spring test application context.

The mock is injected wherever the corresponding bean is required by the test context.

In this example, the controller receives the mocked `GreetingService`.

This is important because the real service bean is not automatically loaded as part of the focused MVC slice.

Without providing the dependency, Spring may be unable to create the controller bean.

### Configuring the Mock

Mockito allows the test to specify the response for a method call:

```java
when(greetingService.greet("Moses")).thenReturn("Hello, Moses!");
```

This means that when the mock receives `greet("Moses")`, it returns `"Hello, Moses!"`.

The test can then exercise the controller without executing the real service implementation.

### Verifying the Interaction

The test also verifies that the controller called the service with the expected argument:

```java
verify(greetingService).greet("Moses");
```

This assertion checks the interaction between the controller and its dependency.

It complements the HTTP response assertions by confirming that the expected service method was invoked.

---

## Understanding MockMvc

`MockMvc` is a Spring testing utility for executing simulated HTTP requests against Spring MVC's request-handling infrastructure.

The example injects it into the test:

```java
@Autowired
private MockMvc mockMvc;
```

A request can then be executed and its response inspected:

```java
mockMvc.perform(get("/greetings"))
        .andExpect(status().isOk())
        .andExpect(content().string("Hello, Spring!"));
```

The main parts are:

| Method                  | Purpose                                |
|-------------------------|----------------------------------------|
| `perform(...)`          | Executes the simulated request.        |
| `get(...)`              | Constructs a GET request.              |
| `param(...)`            | Adds a request parameter.              |
| `andExpect(...)`        | Defines an assertion about the result. |
| `status().isOk()`       | Verifies HTTP status `200 OK`.         |
| `content().string(...)` | Verifies the response body.            |

`MockMvc` does not require a real web server to be started for this test.

---

## Spring Test Slices vs Full Application Context Testing

The main difference between this example and the previous Spring MVC testing example is the scope of the application context.

### MVC Slice Test

```java
@WebMvcTest(GreetingController.class)
public class GreetingControllerTest {
}
```

This loads an MVC-focused test context and uses a mocked service dependency.

It is appropriate when the objective is to verify controller behavior without testing the real service implementation.

### Full Application Context Test

```java
@SpringBootTest
@AutoConfigureMockMvc
public class GreetingControllerTest {
}
```

This loads the full Spring Boot application context and configures `MockMvc`.

When the real service bean is available, requests can exercise the controller and service together.

This is useful for integration tests that verify interactions between multiple application components.

### Comparison

| Feature                | `@WebMvcTest`                              | `@SpringBootTest` + `@AutoConfigureMockMvc`     |
|------------------------|--------------------------------------------|-------------------------------------------------|
| Context scope          | MVC-focused                                | Full Spring Boot application context            |
| Controller testing     | Yes                                        | Yes                                             |
| Service implementation | Commonly mocked                            | Real bean when configured                       |
| Database configuration | Not loaded as part of the normal MVC slice | Loaded when included in the application context |
| Main purpose           | Focused web-layer testing                  | Application integration testing                 |
| Request execution      | `MockMvc`                                  | `MockMvc`                                       |

Neither approach replaces the other. They answer different testing questions.

---

## When Should You Use Spring Test Slices?

Test slices are particularly useful when:

- You want to test one application layer in isolation.
- Loading the full application context is unnecessary.
- You want predictable responses from mocked dependencies.
- You need to test request mappings, parameter binding, or response behavior.
- You want failures to be easier to associate with the layer under test.

Full-context tests remain useful when the objective is to verify that real components work together.

A balanced test suite can use focused tests for individual layers and integration tests for important interactions across the application.

---

## Running the Tests

From the repository root, run:

```bash
mvn -pl 05-spring-testing/spring-test-slices test
```

To verify the module and its required Maven dependencies, run:

```bash
mvn -pl 05-spring-testing/spring-test-slices -am clean verify
```

The tests verify:

- The default query parameter behavior.
- Query parameter binding for supplied names.
- Successful HTTP response status codes.
- Expected response bodies.
- Calls made by the controller to the mocked service.

---

## Key Takeaways

- Spring Test Slices load a focused test context for a particular application layer.
- `@WebMvcTest` is designed for Spring MVC controller testing.
- Specifying the controller class identifies the intended MVC test scope.
- `MockMvc` simulates HTTP requests without starting a real web server.
- `@MockitoBean` provides a mock dependency in the Spring test context.
- Mockito can configure expected responses with `when(...).thenReturn(...)`.
- Mockito can verify interactions with `verify(...)`.
- MVC slice tests focus on web-layer behavior rather than the real service implementation.
- `@SpringBootTest` is more appropriate when a test needs the full application context.
- Slice tests and integration tests complement one another.

---

## What This Example Does Not Cover

This example intentionally focuses on basic MVC slice testing.

It does not cover:

- JSON request and response serialization.
- Request body validation.
- Controller advice and exception handling.
- Spring Security testing.
- Custom MVC configuration.
- Database and repository slice testing.
- `@DataJpaTest`.
- `@JsonTest`.
- Mocking multiple service dependencies.
- Full application integration testing.
- Testing through a real HTTP server.

These topics can be explored in separate examples as the Spring Testing module progresses.

---

## Next Example

The next example will explore **Spring MockMvc** in more detail, including constructing HTTP requests, testing headers and content types, working with request bodies, and verifying more aspects of HTTP responses.
