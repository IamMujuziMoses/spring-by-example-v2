# Spring MVC Testing

This example demonstrates **Spring MVC testing** using Spring Boot's testing support and `MockMvc`.

It focuses on how to test Spring MVC controllers, simulate HTTP requests without starting a real web server, verify response status codes and response bodies, and test query parameter handling in a Spring Boot application.

## What This Example Demonstrates

- What Spring MVC testing means.
- Testing REST controllers with `MockMvc`.
- Using `@SpringBootTest` to load the Spring Boot application context.
- Using `@AutoConfigureMockMvc` to configure `MockMvc`.
- Simulating HTTP GET requests.
- Testing request parameters with `@RequestParam`.
- Testing default values for request parameters.
- Verifying HTTP response status codes.
- Verifying response body content.
- Understanding the difference between controller testing and testing through a real HTTP server.

---

## What Is Spring MVC Testing?

Spring MVC testing verifies the behavior of Spring MVC components, such as controllers, request mappings, request parameter binding, and HTTP responses.

Instead of starting a real web server and sending network requests, tests can use `MockMvc` to simulate HTTP requests and inspect the responses produced by Spring MVC.

For example, consider an endpoint that accepts a name and returns a greeting:

```text
HTTP GET Request
      │
      ▼
/greetings?name=Moses
      │
      ▼
GreetingController
      │
      ▼
GreetingService
      │
      ▼
HTTP Response
      │
      ▼
Hello, Moses!
```

Spring MVC processes the simulated request through its request-handling infrastructure, allowing the test to verify the controller's behavior.

---

### `SpringMvcTestingApplication`

The application entry point uses `@SpringBootApplication` to configure the Spring Boot application.

```java
@SpringBootApplication
public class SpringMvcTestingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMvcTestingApplication.class, args);
    }
}
```

This annotation enables Spring Boot auto-configuration and component scanning.

The application context discovers the controller and service, making them available for dependency injection.

### `GreetingService`

The service provides greeting messages:

```java
@Service
public class GreetingService {

    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
```

The service contains the greeting logic, while the controller handles HTTP requests.

Separating these responsibilities makes the application easier to understand and test.

### `GreetingController`

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

When a name is supplied, the controller passes it to the service. When the parameter is omitted, Spring MVC uses the default value, `"Spring"`.

The endpoint therefore supports requests such as:

```text
GET /greetings
GET /greetings?name=Moses
GET /greetings?name=Java
```

---

## What Is MockMvc?

`MockMvc` is a Spring testing utility that allows tests to exercise Spring MVC request handling without starting a real HTTP server.

It provides an API for constructing requests, executing them through the MVC infrastructure, and asserting the resulting responses.

A typical test follows this structure:

```text
Construct Request
       │
       ▼
Execute with MockMvc
       │
       ▼
Process Through Spring MVC
       │
       ▼
Inspect HTTP Response
       │
       ▼
Assert Expected Behavior
```

For example:

```java
mockMvc.perform(get("/greetings"))
        .andExpect(status().isOk())
        .andExpect(content().string("Hello, Spring!"));
```

This test sends a simulated GET request to `/greetings` and verifies that the response has an HTTP 200 status and the expected body.

---

## Configuring Spring MVC Tests

The example uses two annotations:

```java
@SpringBootTest
@AutoConfigureMockMvc
public class GreetingControllerTest {
}
```

### `@SpringBootTest`

`@SpringBootTest` loads the Spring Boot application context for testing.

This allows the test to use the application's actual configuration and discover Spring-managed components, including the controller and service.

It is useful when a test needs to verify that application components work together within the Spring Boot context.

### `@AutoConfigureMockMvc`

`@AutoConfigureMockMvc` configures `MockMvc` for the test application context.

The test can then inject and use it:

```java
@Autowired
private MockMvc mockMvc;
```

This provides access to the MVC testing API without requiring a real web server to be started.

### Why Use Both Annotations?

The annotations serve different purposes:

| Annotation              | Purpose                                            |
|-------------------------|----------------------------------------------------|
| `@SpringBootTest`       | Loads the Spring Boot application context.         |
| `@AutoConfigureMockMvc` | Configures `MockMvc` to test MVC request handling. |

Together, they allow the example to test the controller using the application's Spring Boot configuration.

---

## Verifying HTTP Responses

Spring MVC tests can verify several aspects of an HTTP response.

### Response Status

```java
.andExpect(status().isOk())
```

This asserts that the response has HTTP status `200 OK`.

### Response Body

```java
.andExpect(content().string("Hello, Moses!"))
```

This asserts that the response body matches the expected string.

These assertions help verify that the controller returns the correct result for a given request.

A test can fail when either the status code or the body differs from the expected value.

---

## MockMvc vs a Real HTTP Server

Spring Boot supports different approaches to testing web applications.

### MockMvc

```text
Test
 │
 ▼
MockMvc
 │
 ▼
Spring MVC Request Handling
 │
 ▼
Controller
 │
 ▼
Response Assertions
```

`MockMvc` tests the MVC request-handling infrastructure without starting a real web server.

It is useful for verifying controller behavior, request mapping, parameter binding, and responses.

### Real HTTP Server

```text
Test Client
    │
    ▼
HTTP Request
    │
    ▼
Running Web Server
    │
    ▼
Spring MVC
    │
    ▼
Controller
    │
    ▼
HTTP Response
```

A real-server test exercises additional infrastructure, including the actual HTTP server and network communication.

For example, `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` can start an embedded server on a random port.

### When Should You Use Each?

| Approach                        | Best suited for                                                             |
|---------------------------------|-----------------------------------------------------------------------------|
| `MockMvc`                       | Testing MVC request handling and controller behavior without a real server. |
| Real-server integration testing | Verifying behavior through an actual HTTP server and client.                |
| Unit testing                    | Testing isolated application logic without loading the Spring context.      |

The best approach depends on which part of the application you need to verify.

---

## Spring MVC Testing vs Test Slices

This example uses `@SpringBootTest` with `@AutoConfigureMockMvc`, which loads the full Spring Boot application context.

Spring Boot also provides `@WebMvcTest` for MVC-focused tests.

A test using `@WebMvcTest` typically loads a restricted set of MVC components rather than the entire application context. Collaborating services can be supplied as mocks.

The distinction is important:

| Feature              | `@SpringBootTest` + `@AutoConfigureMockMvc` | `@WebMvcTest`            |
|----------------------|---------------------------------------------|--------------------------|
| Application context  | Full Spring Boot context                    | MVC-focused test context |
| Controller testing   | Yes                                         | Yes                      |
| Service dependencies | Actual beans when configured                | Commonly mocked          |
| Typical purpose      | Integration testing                         | MVC slice testing        |

This example uses the full application context to demonstrate how the controller and service work together.

A separate test-slice example can explore `@WebMvcTest` and service mocking in more detail.

---

## Running the Example

From the repository root, run:

```bash
mvn -pl 05-spring-testing/spring-mvc-testing -am spring-boot:run
```

This starts the application using the Spring Boot Maven plugin.

Once the application is running, the endpoint can be accessed at:

```text
http://localhost:8080/greetings
http://localhost:8080/greetings?name=Moses
```

Expected responses:

```text
GET /greetings
Hello, Spring!

GET /greetings?name=Moses
Hello, Moses!
```

Stop the application when you have finished experimenting with the endpoints.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or run them from the repository root:

```bash
mvn -pl 05-spring-testing/spring-mvc-testing test
```

To verify the module and its required Maven dependencies from the repository root, run:

```bash
mvn -pl 05-spring-testing/spring-mvc-testing -am clean verify
```

The tests verify:

- The default greeting when no query parameter is supplied.
- The greeting returned for a supplied name.
- Successful HTTP response status codes.
- The expected response body for each request.

---

## Key Takeaways

- Spring MVC testing verifies controller request handling and HTTP responses.
- `MockMvc` simulates HTTP requests without starting a real web server.
- `@SpringBootTest` loads the Spring Boot application context.
- `@AutoConfigureMockMvc` configures `MockMvc` for the test context.
- `@RequestParam` binds query parameters to controller method arguments.
- `defaultValue` supplies a value when a request parameter is omitted.
- `status().isOk()` verifies an HTTP 200 response.
- `content().string(...)` verifies the response body.
- `MockMvc` and real-server integration tests serve different testing purposes.
- `@WebMvcTest` provides an alternative for focused MVC slice testing.

---

## What This Example Does Not Cover

This example intentionally focuses on basic Spring MVC testing with `MockMvc`.

It does not cover:

- `@WebMvcTest` and test slices in detail.
- Mocking controller dependencies.
- JSON request and response testing.
- Request body validation.
- Exception handling and error responses.
- Spring Security integration testing.
- File uploads and multipart requests.
- Asynchronous MVC requests.
- Testing through a real HTTP server.
- Testing REST APIs with external HTTP clients.

These topics can be explored in separate examples as the Spring Testing module progresses.

---

## Next Example

The next example will explore **Spring Test Slices**, including `@WebMvcTest`, how slice tests limit the application context to a specific layer, and how to test controllers with mocked dependencies.
