# MockMvc

This example demonstrates **MockMvc** in Spring MVC testing.

It focuses on how MockMvc simulates HTTP requests without starting a real HTTP server, how Spring MVC processes those requests, and how tests verify HTTP responses using request builders and result matchers.

## What This Example Demonstrates

- What MockMvc is and why it is useful for testing Spring MVC applications.
- Testing controllers without starting a real HTTP server.
- Configuring MockMvc with `MockMvcBuilders.standaloneSetup()`.
- Simulating HTTP GET requests.
- Supplying query parameters.
- Testing default request parameter values.
- Verifying HTTP status codes.
- Asserting response bodies.
- Verifying response content types.
- Understanding standalone controller testing versus Spring context-based testing.

---

## What Is MockMvc?

MockMvc is a Spring testing utility that allows tests to exercise Spring MVC request handling without starting a real HTTP server.

It simulates HTTP requests and allows tests to inspect the resulting responses.

A simplified flow looks like:

```text
Mock HTTP Request
       |
       v
     MockMvc
       |
       v
Spring MVC Request Processing
       |
       v
  Controller Method
       |
       v
   HTTP Response
       |
       v
 Response Assertions
```

This allows developers to test controller behavior without opening a network port or making actual HTTP requests over a network.

---

## The Greeting Controller

The example exposes a `GET /greetings` endpoint.

The controller accepts an optional `name` query parameter and returns a greeting.

```java
@RestController
public class GreetingController {

    @GetMapping("/greetings")
    public String greet(@RequestParam(defaultValue = "Spring") String name) {
        return "Hello, " + name + "!";
    }
}
```

The `@RestController` annotation identifies the class as a REST controller. The `@GetMapping` annotation maps HTTP GET requests to the `greet()` method.

The `@RequestParam` annotation binds a query parameter to the method argument. Its `defaultValue` attribute specifies the value to use when the parameter is omitted.

For example:

```http
GET /greetings
```

Returns:

```text
Hello, Spring!
```

A request that supplies a name:

```http
GET /greetings?name=Moses
```

Returns:

```text
Hello, Moses!
```

---

## Configuring MockMvc

This example uses standalone setup:

```java
@BeforeEach
void setUp() {
    GreetingController controller = new GreetingController();
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
}
```

The controller is instantiated directly and registered with MockMvc.

The `standaloneSetup()` method creates a focused MVC testing environment around the supplied controller.

This approach does not require a Spring application context or a `@SpringBootApplication` class.

The `@BeforeEach` annotation ensures that MockMvc is configured before each test method executes.

---

## Performing HTTP Requests

MockMvc uses `perform()` to execute simulated HTTP requests.

The `get()` request builder creates an HTTP GET request.

### Request Without Parameters

```java
mockMvc.perform(get("/greetings"))
        .andExpect(status().isOk())
        .andExpect(content().string("Hello, Spring!"));
```

The test verifies that the endpoint returns HTTP `200 OK` and the expected default greeting.

### Request With a Query Parameter

```java
mockMvc.perform(get("/greetings").param("name", "Moses"))
        .andExpect(status().isOk())
        .andExpect(content().string("Hello, Moses!"));
```

The `param()` method supplies the query parameter to the simulated request.

This allows the test to verify parameter binding and the controller's response without constructing a real HTTP request.

---

## Verifying HTTP Responses

MockMvc provides result matchers for asserting different aspects of an HTTP response.

### HTTP Status

```java
.andExpect(status().isOk())
```

Verifies that the response status is `200 OK`.

### Response Body

```java
.andExpect(content().string("Hello, Moses!"))
```

Verifies that the response body exactly matches the expected string.

### Content Type

```java
.andExpect(content().contentTypeCompatibleWith(TEXT_PLAIN))
```

Verifies that the response content type is compatible with `text/plain`.

The compatibility matcher allows compatible media types, including ones with additional parameters such as a character encoding.

These assertions help verify not only what a controller returns but also how its response is represented.

---

## Standalone MockMvc vs Spring Context-Based Testing

MockMvc can be configured in different ways depending on what the test needs to exercise.

### Standalone Setup

```java
mockMvc = MockMvcBuilders
        .standaloneSetup(new GreetingController())
        .build();
```

Standalone setup registers controller instances directly.

It is useful for focused tests of request mappings, parameter binding, controller invocation, and response handling.

It does not automatically load the application's complete Spring MVC configuration or all application components.

### Spring Context-Based Setup

A Spring Boot test can use:

```java
@SpringBootTest
@AutoConfigureMockMvc
public class GreetingControllerTest {
    // MockMvc tests
}
```

This approach loads the Spring application context and configures MockMvc using the application's Spring-managed components and applicable configuration.

It is useful when testing behavior that depends on application configuration, filters, interceptors, or other Spring-managed components.

### Comparison

| Feature                          | Standalone Setup         | Spring Context-Based                      |
|----------------------------------|--------------------------|-------------------------------------------|
| Loads Spring application context | No                       | Yes                                       |
| Requires an application class    | No                       | Typically                                 |
| Starts a real HTTP server        | No                       | Not with the default mock web environment |
| Setup complexity                 | Lower                    | Higher                                    |
| Best suited for                  | Focused controller tests | Tests involving application configuration |

Both approaches can test MVC behavior without starting a real HTTP server. The appropriate choice depends on the behavior being tested.

---

## MockMvc vs Real HTTP Testing

MockMvc and real HTTP integration tests serve different purposes.

### MockMvc

```text
Test
 |
 v
MockMvc
 |
 v
Spring MVC
 |
 v
Controller
```

MockMvc exercises Spring MVC request processing in a test environment without opening a network port.

### Real HTTP Testing

```text
Test Client
    |
    v
HTTP Request
    |
    v
Running Web Server
    |
    v
Spring MVC
    |
    v
Controller
```

A real HTTP integration test communicates with a running server and can verify behavior that depends on the actual network and server environment.

MockMvc is generally suitable for focused MVC tests, while real HTTP tests are useful when the server boundary itself matters.

---

## Testing the Example

The example verifies four behaviors:

- The default greeting is returned when no query parameter is supplied.
- A supplied name is used in the greeting.
- Another supplied name is handled correctly.
- The response has a plain-text-compatible content type.

Each test performs a simulated HTTP request and asserts the expected result.

Because the controller is instantiated directly, these tests do not require a Spring Boot application context.

---

## Running the Tests

From the repository root:

```bash
mvn -pl 05-spring-testing/mockmvc test
```

To build the module and its required reactor dependencies:

```bash
mvn -pl 05-spring-testing/mockmvc -am verify
```

---

## Key Takeaways

- MockMvc tests Spring MVC request handling without starting a real HTTP server.
- `MockMvcBuilders.standaloneSetup()` provides a focused way to test controllers.
- Standalone MockMvc does not require an application class.
- `perform()` executes simulated HTTP requests.
- Request builders such as `get()` and `param()` construct test requests.
- Result matchers verify HTTP status codes, response bodies, and content types.
- Standalone setup and Spring context-based setup serve different testing needs.
- MockMvc tests are not equivalent to tests against a real running web server.

---

## What This Example Does Not Cover

This example intentionally focuses on basic MockMvc usage with standalone controller setup.

It does not cover:

- `@WebMvcTest` and MVC test slices.
- `@SpringBootTest` with `@AutoConfigureMockMvc`.
- Spring Security filter chains and security testing.
- Custom argument resolvers and MVC configuration.
- Exception handling and validation.
- File uploads and multipart requests.
- Asynchronous MVC request processing.
- Real HTTP integration testing with a running server.

These topics can be explored in separate examples as needed.

---

## Related Examples

- [Spring MVC Testing](../spring-mvc-testing/README.md)
- [Spring Test Slices](../spring-test-slices/README.md)
- [Spring Boot Test](../spring-boot-test/README.md)

---

## Next Example

**Transaction Testing**

The next example will explore another Spring Testing topic, building on the fundamentals of controller testing and application context testing.
