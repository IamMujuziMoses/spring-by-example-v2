# Application Testing

This example demonstrates **application testing** in Spring Boot.

It focuses on testing a complete Spring Boot application, verifying that the application context starts successfully, that application components are correctly wired, and that HTTP requests produce the expected responses.

The example uses `@SpringBootTest` together with `@AutoConfigureMockMvc` to exercise the application through its HTTP endpoints without starting a real embedded web server.

## What This Example Demonstrates

- What application testing means in Spring Boot.
- The difference between unit testing, slice testing, and application testing.
- The `@SpringBootTest` annotation.
- Loading the complete Spring application context.
- Testing application context initialization.
- The `@AutoConfigureMockMvc` annotation.
- Using `MockMvc` with a full application context.
- Testing HTTP endpoints and request parameters.
- Verifying JSON response serialization.
- Testing the interaction between controllers and services.
- The difference between mock web environment testing and real HTTP server testing.

---

## What Is Application Testing?

Application testing verifies that an application works correctly when its components are assembled and configured together.

Unlike isolated unit tests, which focus on individual classes, application tests verify the behavior of multiple components working together within the Spring application context.

For example, consider an application containing:

- A REST controller.
- A service.
- Spring dependency injection.
- HTTP request mapping.
- JSON response serialization.

An application test can verify that these components work together as expected.

A simplified application flow looks like:

```text
HTTP Request
     │
     ▼
Spring Application Context
     │
     ▼
Controller
     │
     ▼
Service
     │
     ▼
Application Logic
     │
     ▼
HTTP Response
```

Application testing helps identify problems that isolated unit tests might not detect, such as incorrect bean configuration, missing dependencies, or integration problems between components.

---

## Application Testing vs Unit Testing

Unit testing and application testing serve different purposes.

### Unit Testing

Unit tests verify individual components in isolation.

For example, a service can be tested directly without starting Spring:

```java
@Test
void greet_shouldReturnPersonalizedGreeting() {
    GreetingService service = new GreetingService();

    Greeting greeting = service.greet("Moses");

    assertEquals("Hello, Moses!", greeting.message());
}
```

This verifies the service logic without requiring a Spring application context.

### Application Testing

Application tests verify that the application works with its components assembled by Spring.

For example:

```java
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationTestingApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void greet_shouldReturnPersonalizedGreeting() throws Exception {
        mockMvc.perform(get("/api/greetings").param("name", "Moses")).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Moses!"));
    }
}
```

This test exercises HTTP request handling, the controller, the service, and response serialization together.

---

## Application Structure

The example implements a simple greeting application.

Its structure is:

```text
ApplicationTestingApplication
          │
          ▼
GreetingController
          │
          ▼
GreetingService
          │
          ▼
Greeting
```

The application exposes a REST endpoint that returns greeting messages.

The endpoint supports an optional `name` request parameter.

When no name is supplied, the application uses `Spring` as the default name.

---

## Main Application Class

The application entry point uses `@SpringBootApplication`:

```java
@SpringBootApplication
public class ApplicationTestingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationTestingApplication.class, args);
    }
}
```

`@SpringBootApplication` enables Spring Boot auto-configuration and component scanning.

It allows Spring to discover the controller and service and register them as application beans.

This configuration is important because the application tests will load the application through this entry point.

---

## Greeting Model

The `Greeting` record represents the response returned by the application:

```java
public record Greeting(String message) {
}
```

For example, a greeting for `Moses` is represented as:

```json
{
  "message": "Hello, Moses!"
}
```

Spring Boot automatically serializes the record into JSON when it is returned from the REST controller.

---

## Greeting Service

The `GreetingService` contains the application's greeting logic:

```java
@Service
public class GreetingService {

    public Greeting greet(String name) {
        return new Greeting("Hello, " + name + "!");
    }
}
```

The service receives a name and creates a corresponding greeting.

It is annotated with `@Service`, allowing Spring to discover and register it as a bean.

The controller uses constructor injection to receive the service.

---

## Greeting Controller

The `GreetingController` exposes the greeting endpoint:

```java
@RestController
@RequestMapping("/api/greetings")
public class GreetingController {

    private final GreetingService greetingService;

    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping
    public Greeting greet(@RequestParam(defaultValue = "Spring") String name) {
        return greetingService.greet(name);
    }
}
```

The endpoint is:

```text
GET /api/greetings
```

It accepts an optional `name` request parameter.

### Default Greeting

When no name is supplied:

```http
GET /api/greetings
```

The application returns:

```json
{
  "message": "Hello, Spring!"
}
```

### Personalized Greeting

When a name is supplied:

```http
GET /api/greetings?name=Moses
```

The application returns:

```json
{
  "message": "Hello, Moses!"
}
```

The `defaultValue` attribute of `@RequestParam` specifies the value used when the request parameter is not provided.

---

## Spring Boot Application Testing

Spring Boot provides `@SpringBootTest` for testing applications using their actual Spring application configuration.

The annotation creates a Spring application context for the test.

For example:

```java
@SpringBootTest
public class ApplicationTestingApplicationTest {

    @Test
    void contextLoads() {
    }
}
```

The test verifies that the application context starts successfully.

If Spring cannot initialize the application context because of a configuration problem, missing bean, or dependency injection failure, the test fails.

### What Does `@SpringBootTest` Load?

By default, `@SpringBootTest` searches for the application's primary configuration class, usually the class annotated with `@SpringBootApplication`.

It then creates an application context using that configuration.

Conceptually:

```text
@SpringBootTest
       │
       ▼
ApplicationTestingApplication
       │
       ▼
Spring Boot Auto-Configuration
       │
       ▼
Component Scanning
       │
       ├── GreetingController
       │
       └── GreetingService
       │
       ▼
Application Context
```

This allows tests to use the actual application beans instead of manually constructing or mocking every dependency.

---

## Testing Application Context Initialization

The example includes:

```java
@Test
void contextLoads() {
}
```

Although the method is empty, it serves an important purpose.

The test passes when Spring successfully initializes the application context.

If application startup fails, the test fails before the method executes.

This makes it useful for detecting problems such as:

- Missing application beans.
- Unsatisfied dependencies.
- Invalid application configuration.
- Bean creation failures.
- Auto-configuration problems.

The test intentionally has no additional assertions because successful context initialization is the behavior being verified.

---

## Testing HTTP Endpoints with MockMvc

The example uses `MockMvc` to test HTTP endpoints.

`MockMvc` provides a way to perform HTTP requests against Spring MVC applications without starting a real HTTP server.

It allows tests to exercise request mapping, controller execution, request parameter handling, exception handling, and response serialization.

To configure it with a full application context, the example uses:

```java
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationTestingApplicationTest {
}
```

### `@AutoConfigureMockMvc`

`@AutoConfigureMockMvc` configures and provides a `MockMvc` instance for the test.

The instance can be injected into the test:

```java
@Autowired
private MockMvc mockMvc;
```

The test can then perform HTTP requests against the application.

### Performing a GET Request

A request can be performed using:

```java
mockMvc.perform(get("/api/greetings")).andExpect(status().isOk());
```

The request is processed through the Spring MVC infrastructure configured in the application context.

---

## Testing the Default Greeting

The example verifies that the endpoint returns the default greeting when no name is supplied.

```java
@Test
void greet_shouldReturnDefaultGreeting() throws Exception {
    mockMvc.perform(get("/api/greetings")).andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Hello, Spring!"));
}
```

The test verifies two things:

1. The endpoint returns HTTP `200 OK`.
2. The JSON response contains the expected default greeting.

This exercises the default value configured on the controller's `@RequestParam`.

---

## Testing Personalized Greetings

The example also verifies that the endpoint accepts a custom name.

```java
@Test
void greet_shouldReturnPersonalizedGreeting() throws Exception {
    mockMvc.perform(get("/api/greetings").param("name", "Moses")).andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Hello, Moses!"));
}
```

The `.param()` method supplies a request parameter.

The test verifies that:

- The request is successfully processed.
- The controller receives the supplied name.
- The service generates the personalized greeting.
- The application returns the expected JSON response.

This demonstrates how application tests can verify behavior across multiple application components.

---

## Testing JSON Responses

Spring Boot uses HTTP message converters to serialize Java objects into HTTP response bodies.

In this example, the controller returns a `Greeting` record, which is serialized into JSON.

The example verifies the response content type and JSON representation:

```java
@Test
void greet_shouldReturnJsonResponse() throws Exception {
    mockMvc.perform(get("/api/greetings")).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""
                    {
                        "message": "Hello, Spring!"
                    }
                    """));
}
```

This test verifies that:

- The response is successful.
- The response content type is compatible with `application/json`.
- The response body contains the expected JSON representation.

This is useful because application behavior includes more than the values returned by business logic. It also includes how those values are exposed over HTTP.

---

## Complete Application Testing Flow

The example uses the following testing arrangement:

```text
ApplicationTestingApplicationTest
             │
             ▼
       @SpringBootTest
             │
             ▼
   Complete Spring Application Context
             │
             ▼
      @AutoConfigureMockMvc
             │
             ▼
           MockMvc
             │
             ▼
      GreetingController
             │
             ▼
       GreetingService
             │
             ▼
          Greeting
             │
             ▼
       JSON Response
             │
             ▼
       Test Assertions
```

The tests exercise the application through its HTTP interface while using the actual Spring-managed application components.

---

## `@SpringBootTest` vs `@WebMvcTest`

Both annotations are useful for testing Spring applications, but they serve different purposes.

### `@SpringBootTest`

Loads the complete application context using the application's configuration.

It is useful when testing interactions between multiple application components or verifying application configuration.

```java
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationTestingApplicationTest {
}
```

### `@WebMvcTest`

Loads a focused Spring MVC test context.

It is useful when testing the web layer independently of the rest of the application.

```java
@WebMvcTest(GreetingController.class)
public class GreetingControllerTest {
}
```

With `@WebMvcTest`, service dependencies are typically supplied through mocks or test bean overrides.

### Comparison

| Feature             | `@SpringBootTest`                        | `@WebMvcTest`                         |
|---------------------|------------------------------------------|---------------------------------------|
| Application context | Full application context by default      | Focused MVC context                   |
| Controller          | Included                                 | Included when selected                |
| Service beans       | Loaded through application configuration | Usually mocked or supplied separately |
| Auto-configuration  | Broad application auto-configuration     | Focused MVC auto-configuration        |
| Main purpose        | Application integration testing          | Web-layer testing                     |
| Startup cost        | Generally higher                         | Generally lower                       |

The choice depends on what the test is intended to verify.

---

## MockMvc vs Real HTTP Server Testing

This example uses `MockMvc` with `@SpringBootTest`.

Although the complete application context is loaded, a real embedded HTTP server is not started.

Spring Boot also supports testing against a running server.

For example:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationHttpTest {
}
```

The `RANDOM_PORT` option starts an embedded server on an available port.

Tests can then send actual HTTP requests to the running application using an HTTP client such as `TestRestTemplate` or `WebTestClient`, depending on the application setup.

The distinction is:

### MockMvc

```text
Test
 │
 ▼
MockMvc
 │
 ▼
Spring MVC Infrastructure
 │
 ▼
Controller
```

### Random Port

```text
Test
 │
 ▼
HTTP Client
 │
 ▼
Embedded HTTP Server
 │
 ▼
Spring MVC Infrastructure
 │
 ▼
Controller
```

`MockMvc` is useful for testing application behavior through Spring MVC without the overhead of a running server.

Real HTTP server testing is useful when the actual server and HTTP communication are also part of what needs to be verified.

---

## Running the Example

From the repository root:

```bash
mvn -pl 06-capstone-application/application-testing spring-boot:run
```

The application starts and exposes the greeting endpoint.

### Default Greeting

```bash
curl http://localhost:8080/api/greetings
```

Expected response:

```json
{
  "message": "Hello, Spring!"
}
```

### Personalized Greeting

```bash
curl "http://localhost:8080/api/greetings?name=Moses"
```

Expected response:

```json
{
  "message": "Hello, Moses!"
}
```

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 06-capstone-application/application-testing test
```

To build the module together with its required upstream modules:

```bash
mvn -pl 06-capstone-application/application-testing -am test
```

---

## Key Takeaways

- Application testing verifies that application components work together.
- `@SpringBootTest` loads the complete Spring application context by default.
- A successful context-loading test verifies that Spring can initialize the application.
- `@AutoConfigureMockMvc` provides `MockMvc` for testing HTTP endpoints.
- `MockMvc` can be used with a full application context without starting a real HTTP server.
- Application tests can verify controller and service interactions.
- Spring MVC handles request mapping, parameter binding, and response serialization.
- JSON response assertions verify the application's HTTP representation.
- `@SpringBootTest` and `@WebMvcTest` serve different testing purposes.
- `RANDOM_PORT` can be used when testing against a running embedded HTTP server.
- The testing approach should match the behavior and application boundaries being verified.

---

## What This Example Does Not Cover

This example intentionally focuses on basic application testing with `@SpringBootTest` and `MockMvc`.

It does not cover:

- Real HTTP server testing with `RANDOM_PORT`.
- `TestRestTemplate`.
- `WebTestClient`.
- Database integration testing.
- Testcontainers.
- External service integration testing.
- Test configuration and profiles.
- Dynamic test properties.
- Application event testing.
- Asynchronous application testing.
- Transaction management in integration tests.

These topics can be explored in separate examples.

---

## Next Example

The next example will explore **Integration Between Spring Technologies**, demonstrating how Spring Boot, Spring Data, Spring Security, Spring Modulith, and transaction management work together within a realistic application.
