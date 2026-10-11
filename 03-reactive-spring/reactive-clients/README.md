# Reactive Clients

## Overview

This example demonstrates how to use Spring WebFlux's `WebClient` to consume external HTTP APIs reactively.

It builds on the concepts introduced in the previous Reactive Spring examples by showing how reactive publishers can represent not only data produced within an application, but also the results of asynchronous HTTP requests to external services.

The example uses JSONPlaceholder as a sample REST API and demonstrates how to configure a reusable `WebClient`, retrieve individual resources and collections, decode JSON responses into Java objects, and compose reactive operations without manually subscribing to publishers.

## What This Example Demonstrates

- Configuring a reusable `WebClient` bean.
- Externalizing the base URL through application properties.
- Making non-blocking HTTP GET requests with `WebClient`.
- Decoding JSON responses into Java records.
- Using `Mono` for a single resource and `Flux` for multiple resources.
- Composing reactive operations with `map()`.
- Handling HTTP error responses using `retrieve()`.
- Exposing reactive client operations through a Spring WebFlux controller.
- Testing HTTP client behavior without making real network requests.
- Verifying reactive behavior using `StepVerifier`.

---

## 1. Understanding WebClient

`WebClient` is Spring's reactive HTTP client. It supports asynchronous, non-blocking communication with remote HTTP services and integrates with Project Reactor through `Mono` and `Flux`.

Unlike a traditional blocking HTTP client, `WebClient` allows an application to compose HTTP operations into a reactive pipeline without immediately waiting for each response.

For example:

```java
webClient.get().uri("/posts/1").retrieve().bodyToMono(Post.class);
```

This operation describes an HTTP GET request and the conversion of its response body into a `Post` object.

The returned `Mono<Post>` represents the result of the request. The actual HTTP exchange occurs when the publisher is subscribed to, for example by a WebFlux runtime handling an incoming HTTP request.

---

## 2. Configuring WebClient

The example defines a reusable `WebClient` bean in `WebClientConfiguration`.

```java
@Configuration
public class WebClientConfiguration {

    @Bean
    WebClient postWebClient(WebClient.Builder builder,
            @Value("${clients.posts.base-url:https://jsonplaceholder.typicode.com}") String baseUrl) {

        return builder.baseUrl(baseUrl).build();
    }
}
```

### How it works

- `@Configuration` identifies a class containing Spring bean definitions.
- `@Bean` registers the returned `WebClient` instance in the Spring application context.
- `WebClient.Builder` provides the builder used to configure the client.
- `@Value` injects the configured base URL, falling back to the default when the property is absent.
- `baseUrl()` establishes the base address used to resolve relative request paths.

The configuration separates HTTP client setup from the code that performs requests. This keeps `PostClient` focused on consuming the remote API.

### Configuring the base URL

The default base URL is:

```text
https://jsonplaceholder.typicode.com
```

It can be overridden in `application.properties`:

```properties
clients.posts.base-url=https://jsonplaceholder.typicode.com
```

For another environment, the same property can point to a different compatible service without changing `PostClient`.

---

## 3. Representing the Response

The `Post` record models the JSON returned by JSONPlaceholder.

```java
public record Post(Long userId, Long id, String title, String body) {
}
```

For a response such as:

```json
{
  "userId": 1,
  "id": 1,
  "title": "Example title",
  "body": "Example body"
}
```

Spring's configured HTTP message readers decode the JSON into a `Post` instance when `bodyToMono(Post.class)` or `bodyToFlux(Post.class)` is used.

The record provides a concise immutable data representation for the response.

---

## 4. Building the Reactive HTTP Client

`PostClient` encapsulates the remote API operations.

### Retrieving all posts

```java
public Flux<Post> findAll() {
    return webClient.get().uri("/posts").retrieve().bodyToFlux(Post.class);
}
```

The method returns a `Flux<Post>` because the endpoint returns a collection of posts.

`bodyToFlux(Post.class)` decodes the JSON array into a stream of individual `Post` objects.

### Retrieving a post by ID

```java
public Mono<Post> findById(Long id) {
    return webClient.get().uri("/posts/{id}", id).retrieve().bodyToMono(Post.class);
}
```

This method returns a `Mono<Post>` because it requests one resource.

The URI template allows the post ID to be supplied as a parameter rather than concatenated into a URL manually.

### Retrieving only the title

```java
public Mono<String> findTitleById(Long id) {
    return findById(id).map(Post::title);
}
```

This method demonstrates reactive composition.

Instead of making another HTTP request or manually extracting the title from a response, it transforms the `Post` emitted by `findById()` into its title.

The `map()` operator applies the transformation when the publisher emits a value.

### Understanding retrieve()

`retrieve()` prepares the response handling pipeline. By default, HTTP responses with status codes in the 4xx or 5xx range are converted into error signals.

For example, a 404 response produces a `WebClientResponseException.NotFound` unless custom status handling is configured.

This error is delivered through the reactive pipeline rather than being handled as a successful `Post` response.

---

## 5. Exposing the Client Through a WebFlux Controller

`PostController` exposes the client operations through application endpoints.

```java
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostClient postClient;

    public PostController(PostClient postClient) {
        this.postClient = postClient;
    }

    @GetMapping
    public Flux<Post> findAll() {
        return postClient.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Post> findById(@PathVariable Long id) {
        return postClient.findById(id);
    }

    @GetMapping("/{id}/title")
    public Mono<String> findTitleById(@PathVariable Long id) {
        return postClient.findTitleById(id);
    }
}
```

The controller delegates HTTP client operations to `PostClient` and returns the resulting publishers directly.

Spring WebFlux subscribes to these publishers as part of processing incoming HTTP requests and writes their results to the outgoing responses.

There is no need to call `subscribe()` manually in the controller.

### Available endpoints

| Method | Endpoint                | Description                |
|--------|-------------------------|----------------------------|
| GET    | `/api/posts`            | Retrieves all posts        |
| GET    | `/api/posts/{id}`       | Retrieves one post by ID   |
| GET    | `/api/posts/{id}/title` | Retrieves one post's title |

For example:

```bash
curl http://localhost:8080/api/posts
```

```bash
curl http://localhost:8080/api/posts/1
```

```bash
curl http://localhost:8080/api/posts/1/title
```

The endpoints return data from the configured remote API. A successful request therefore depends on the external service being reachable.

---

## 6. Understanding the Request Flow

The example separates incoming HTTP requests from outgoing HTTP requests.

```text
Incoming HTTP Request
        |
        v
PostController
        |
        v
PostClient
        |
        v
WebClient
        |
        v
External REST API
        |
        v
HTTP Response
        |
        v
JSON Decoding
        |
        v
Mono<Post> / Flux<Post>
        |
        v
WebFlux HTTP Response
```

The controller exposes the application endpoint, while the client handles communication with the external service.

The returned reactive publisher represents the result of that communication. WebFlux handles subscription and response writing as part of its request-processing lifecycle.

---

## 7. Testing Reactive Clients

The example uses JUnit, AssertJ, and Project Reactor's `StepVerifier` to test the client behavior.

The tests use a custom `ExchangeFunction` to simulate HTTP responses:

```java
WebClient webClient = WebClient.builder()
        .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(responseBody)
                        .build()))
        .build();

PostClient postClient = new PostClient(webClient);
```

This allows the tests to exercise response decoding and reactive transformations without calling JSONPlaceholder.

### What the tests verify

- `findById_shouldDecodeResponseIntoPost()` verifies that a JSON response is decoded into a `Post`.
- `findAll_shouldDecodeResponseIntoPostSequence()` verifies that a JSON array becomes a sequence of `Post` values.
- `findTitleById_shouldTransformPostIntoTitle()` verifies the transformation from a `Post` to its title.
- `findById_shouldSignalErrorForNotFoundResponse()` verifies that a 404 response produces the expected reactive error.

### Why use StepVerifier?

`StepVerifier` allows tests to express expectations about the signals emitted by a reactive publisher.

For example:

```java
StepVerifier.create(postClient.findById(1L))
        .assertNext(post -> {
            assertThat(post.id()).isEqualTo(1L);
            assertThat(post.title()).isEqualTo("Example title");
        })
        .verifyComplete();
```

This verifies that the publisher emits the expected post and then completes successfully.

For the error case:

```java
StepVerifier.create(postClient.findById(999L))
        .expectError(WebClientResponseException.NotFound.class)
        .verify();
```

This verifies the error signal rather than expecting an ordinary response value.

The mock exchange function keeps these tests deterministic and independent of remote API availability.

---

## 8. Key Concepts

### WebClient

A reactive HTTP client for communicating with external services.

### Mono

Represents a publisher that emits zero or one value. It is appropriate for retrieving a single post or title.

### Flux

Represents a publisher that can emit zero or more values. It is appropriate for retrieving a collection of posts.

### Reactive composition

Operators such as `map()` transform values emitted by publishers without requiring manual subscription.

### Response decoding

`bodyToMono()` and `bodyToFlux()` convert HTTP response bodies into the requested Java types.

### Error signals

`retrieve()` converts 4xx and 5xx HTTP responses into reactive errors by default.

### Dependency injection

Spring provides the configured `WebClient` bean to `PostClient`, keeping client configuration separate from request logic.

---

## 9. What This Example Does Not Cover

This example intentionally focuses on the fundamentals of reactive HTTP clients. It does not cover:

- Authentication and authorization with external services.
- Custom retry policies and exponential backoff.
- Request timeouts and connection pool tuning.
- Custom error mapping with `onStatus()`.
- Circuit breakers and resilience patterns.
- Streaming large response bodies.
- OAuth2 client configuration.
- Integration tests against a live external service.
- Advanced observability and distributed tracing.

These topics can be explored in separate examples once the core concepts are understood.

---

## 10. Running the Example

From the repository root, run the tests:

```bash
mvn -pl 03-reactive-spring/reactive-clients clean verify
```

Run the application:

```bash
mvn -pl 03-reactive-spring/reactive-clients spring-boot:run
```

The application starts on the default Spring Boot port, `8080`, unless configured otherwise.

Try the endpoints:

```bash
curl http://localhost:8080/api/posts
curl http://localhost:8080/api/posts/1
curl http://localhost:8080/api/posts/1/title
```

The application needs network access to reach the configured remote service. The unit tests do not require that service to be available.

---

## Key Takeaways

- `WebClient` enables reactive HTTP communication with external services.
- `WebClientConfiguration` centralizes client setup and makes the base URL configurable.
- `Mono` and `Flux` represent the results of individual-resource and collection requests.
- `bodyToMono()` and `bodyToFlux()` decode JSON responses into Java objects.
- Operators such as `map()` make it possible to compose transformations into reactive pipelines.
- `retrieve()` converts unsuccessful HTTP status codes into error signals by default.
- Spring WebFlux handles publisher subscription when serving incoming HTTP requests.
- A custom `ExchangeFunction` makes it possible to test HTTP client behavior without a live remote API.

---

## Next Example

**Reactive data access**

The next example will build on reactive HTTP communication by exploring additional patterns for composing, handling, and testing reactive operations.
