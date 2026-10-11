# WebFlux

This example demonstrates the fundamentals of Spring WebFlux by exposing reactive HTTP endpoints that return `Mono` and `Flux`.

Spring WebFlux is Spring's reactive web framework. It supports non-blocking request processing and integrates with Reactive Streams publishers, including Project Reactor's `Mono` and `Flux`.

The goal of this example is to understand how reactive types are used at the web layer and how to test WebFlux controllers.

## What This Example Demonstrates

- Configuring a Spring Boot WebFlux application
- Creating reactive REST controllers
- Returning `Mono<T>` from an HTTP endpoint
- Returning `Flux<T>` from an HTTP endpoint
- Binding path variables and query parameters
- Serializing reactive results into HTTP responses
- Testing WebFlux endpoints with `WebTestClient`
- Testing a controller without starting a full HTTP server

---

## What Is Spring WebFlux?

Spring WebFlux is a web framework designed to support reactive and non-blocking request processing.

It integrates with Project Reactor and supports reactive return types such as `Mono` and `Flux`.

A traditional controller might return a value directly:

```java
@GetMapping("/{name}")
public Greeting greet(@PathVariable String name) {
    return greetingService.greet(name);
}
```

A WebFlux controller can instead return a publisher:

```java
@GetMapping("/{name}")
public Mono<Greeting> greet(@PathVariable String name) {
    return greetingService.greet(name);
}
```

The second method returns a `Mono<Greeting>`. WebFlux subscribes to the publisher as part of handling the request and writes the emitted value to the HTTP response.

Returning a reactive type does not automatically make every operation non-blocking. The operations performed inside the publisher must also avoid blocking if the application is to benefit from non-blocking execution.

---

## The Application

The application consists of four main components:

- `WebFluxApplication` — starts the Spring Boot application.
- `GreetingController` — exposes the HTTP endpoints.
- `GreetingService` — produces reactive greeting results.
- `Greeting` — represents the response payload.

The controller delegates to the service and returns the publisher to WebFlux.

---

## Returning a Mono

`Mono<T>` represents a publisher that emits at most one value, completes empty, or terminates with an error.

The greeting service creates a `Mono<Greeting>`:

```java
public Mono<Greeting> greet(String name) {
    return Mono.just(createGreeting(name));
}
```

The controller exposes it through an HTTP endpoint:

```java
@GetMapping("/{name}")
public Mono<Greeting> greet(@PathVariable String name) {
    return greetingService.greet(name);
}
```

Example request:

```http
GET /api/greetings/Alice
```

Example response:

```json
{
  "message": "Hello, Alice!"
}
```

The `Mono` provides the response value to the WebFlux infrastructure, which serializes the greeting as JSON.

---

## Returning a Flux

`Flux<T>` represents a publisher that can emit zero, one, or many values.

The service accepts a list of names and creates a publisher from it:

```java
public Flux<Greeting> greetAll(List<String> names) {
    return Flux.fromIterable(names).map(this::createGreeting);
}
```

The controller exposes the publisher through a collection endpoint:

```java
@GetMapping
public Flux<Greeting> greetAll(@RequestParam(name = "names") List<String> names) {
    return greetingService.greetAll(names);
}
```

Example request:

```http
GET /api/greetings?names=Alice&names=Bob
```

Example response:

```json
[
  {
    "message": "Hello, Alice!"
  },
  {
    "message": "Hello, Bob!"
  }
]
```

WebFlux writes the values emitted by the `Flux` as a JSON array for this endpoint.

A `Flux` does not necessarily mean that all values are produced asynchronously or that they are delivered one at a time over the network. The source publisher and the configured HTTP message writer determine how the values are produced and serialized.

---

## Request Processing Flow

A simplified view of a request is:

```text
HTTP Request
     |
     v
WebFlux Infrastructure
     |
     v
GreetingController
     |
     v
GreetingService
     |
     v
Mono<Greeting> or Flux<Greeting>
     |
     v
Subscription and Signal Processing
     |
     v
HTTP Message Writer
     |
     v
HTTP Response
```

The controller returns a publisher rather than manually subscribing to it.

This allows WebFlux to manage the publisher as part of the HTTP request lifecycle.

---

## Why Controllers Should Not Subscribe Manually

A WebFlux controller should generally return the publisher it wants the framework to handle:

```java
@GetMapping("/{name}")
public Mono<Greeting> greet(@PathVariable String name) {
    return greetingService.greet(name);
}
```

Avoid subscribing inside the controller and returning an unrelated value:

```java
// Avoid this pattern in a reactive controller.
@GetMapping("/{name}")
public String greet(@PathVariable String name) {
    greetingService.greet(name).subscribe();
    
    return "Request received";
}
```

Manually subscribing creates a separate subscription and disconnects the publisher's result from the HTTP response lifecycle. It can also make error handling and request cancellation harder to manage.

Returning the publisher lets WebFlux coordinate subscription, response writing, and the reactive signals associated with the request.

---

## Testing with WebTestClient

`WebTestClient` is designed for testing web applications built with Spring WebFlux. It can exercise endpoints using mock request and response infrastructure without starting a full HTTP server.

The test uses `@WebFluxTest` to load the controller and WebFlux infrastructure:

```java
@WebFluxTest(GreetingController.class)
@Import(GreetingService.class)
public class GreetingControllerTest {

    @Autowired
    private WebTestClient webTestClient;
}
```

`@Import(GreetingService.class)` supplies the service used by the controller.

A test can issue a request and verify the response:

```java
webTestClient.get().uri("/api/greetings/Alice").exchange().expectStatus().isOk().expectBody()
        .jsonPath("$.message")
        .isEqualTo("Hello, Alice!");
```

This verifies both the HTTP status and the JSON response body.

The collection endpoint is tested by supplying repeated query parameters and checking the serialized array:

```java
webTestClient.get().uri("/api/greetings?names=Alice&names=Bob").exchange().expectStatus().isOk().expectBody()
        .jsonPath("$[0].message")
        .isEqualTo("Hello, Alice!")
        .jsonPath("$[1].message")
        .isEqualTo("Hello, Bob!");
```

The tests also verify that an unknown route returns `404 Not Found`.

---

## Key Concepts

### `Mono<T>`

A publisher that can emit at most one value, complete empty, or signal an error.

### `Flux<T>`

A publisher that can emit multiple values, complete empty, or signal an error.

### `@RestController`

Declares a controller whose return values are written to HTTP responses.

### `@GetMapping`

Maps HTTP GET requests to controller methods.

### Reactive Return Types

Returning a `Mono` or `Flux` allows WebFlux to integrate publisher signals with request processing and response writing.

### `WebTestClient`

Provides an API for issuing HTTP-style requests and asserting response statuses, headers, and bodies in tests.

### Non-Blocking Processing

WebFlux supports non-blocking request handling, but the application must also avoid blocking operations in its reactive execution paths to preserve that benefit.

---

## What This Example Does Not Cover

This example intentionally does not cover:

- Functional endpoints using `RouterFunction` and `HandlerFunction`
- Reactive database access with R2DBC
- Calling external services with `WebClient`
- Reactive validation and custom exception handling
- Reactive security
- Server-Sent Events or streaming response formats
- Advanced scheduler management
- Production deployment and performance tuning

Those topics can be explored in separate examples.

---

## Running the Example

From the repository root, run:

```bash
mvn -pl 03-reactive-spring/webflux spring-boot:run
```

The application starts using Spring Boot's configured WebFlux server.

Try the endpoints:

```bash
curl http://localhost:8080/api/greetings/Alice
```

```bash
curl "http://localhost:8080/api/greetings?names=Alice&names=Bob"
```

---

## Running Tests

Run the module's tests from the repository root:

```bash
mvn -pl 03-reactive-spring/webflux test
```

Or run the full verification lifecycle:

```bash
mvn -pl 03-reactive-spring/webflux clean verify
```

---

## Key Takeaways

- Spring WebFlux integrates reactive publishers with HTTP request processing.
- `Mono` is useful for endpoints that return at most one value.
- `Flux` is useful for endpoints that produce a sequence of values.
- WebFlux can serialize reactive results into HTTP responses.
- Controllers should normally return publishers rather than subscribe manually.
- `WebTestClient` makes it possible to test reactive endpoints without starting a full server.
- A reactive return type alone does not guarantee that the work performed by an application is non-blocking.

---

## Next Example

**Reactive controllers**

The next example will build on WebFlux fundamentals by exploring additional reactive web capabilities and how they fit into a Spring application.
