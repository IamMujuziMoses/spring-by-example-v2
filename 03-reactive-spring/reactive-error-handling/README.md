# Reactive Error Handling

This module demonstrates how to handle errors in reactive applications using **Project Reactor and Spring WebFlux**.

In reactive programming, errors are not handled in exactly the same way as exceptions in traditional synchronous code. Errors are signals that travel through a reactive pipeline, and operators determine whether those errors should be recovered from, transformed, observed, or propagated to the subscriber.

The goal of this example is to understand how reactive error handling works, how to create meaningful fallback behavior, and how to translate application exceptions into appropriate HTTP responses.

## Learning Objectives

By completing this example, you should understand:

- How errors propagate through `Mono` and `Flux`.
- How to provide fallback values with `onErrorReturn()`.
- How to recover from failures using `onErrorResume()`.
- How to transform exceptions using `onErrorMap()`.
- How to observe failures using `doOnError()`.
- How to define custom application exceptions.
- How to centralize exception handling in Spring WebFlux.
- How to map application errors to meaningful HTTP status codes.
- How to test reactive error handling with `StepVerifier` and `WebTestClient`.

---

## 1. Understanding Reactive Errors

A reactive pipeline can emit three types of signals:

- **Next:** Emits an item to the subscriber.
- **Complete:** Indicates that the sequence has completed successfully.
- **Error:** Indicates that the sequence has terminated because of a failure.

A reactive sequence terminates when it emits either a completion signal or an error signal.

For example:

```java
Mono<String> operation = Mono.error(
    new IllegalStateException("Operation failed")
);
```

This `Mono` does not emit a value. It emits an error signal containing an `IllegalStateException`.

Reactive operators can intercept that error signal and determine what happens next.

---

## 2. Handling Errors with Reactor Operators

Project Reactor provides several operators for handling errors in reactive pipelines. Although they all work with error signals, each operator serves a different purpose.

### 2.1. `onErrorReturn()`

`onErrorReturn()` replaces an error signal with a predefined fallback value.

```java
public Mono<String> demonstrateOnErrorReturn() {
    return Mono.<String>error(new IllegalStateException("Primary operation failed")).onErrorReturn("Fallback response");
}
```

If the upstream operation fails, the subscriber receives:

```text
Fallback response
```

The sequence then completes successfully.

**When to use it:** When a fixed fallback value is appropriate and recovering does not require additional processing.

For example, an application might return a predefined message when optional information cannot be retrieved.

### 2.2. `onErrorResume()`

`onErrorResume()` allows you to recover from an error by switching to another reactive publisher.

```java
public Mono<String> demonstrateOnErrorResume() {
    return Mono.<String>error(new IllegalStateException("Primary operation failed"))
        .onErrorResume(IllegalStateException.class, error -> Mono.just("Recovered from: " + error.getMessage()));
}
```

The resulting value is:

```text
Recovered from: Primary operation failed
```

Unlike `onErrorReturn()`, the fallback can depend on the exception and can perform another reactive operation.

**When to use it:** When recovery requires a fallback operation, an alternative data source, or error-dependent behavior.

### 2.3. `onErrorMap()`

`onErrorMap()` transforms one exception into another while preserving the error signal.

```java
public Mono<String> demonstrateOnErrorMap() {
    return Mono.<String>error(new IllegalStateException("Database operation failed"))
            .onErrorMap(IllegalStateException.class, error -> 
                new ServiceOperationException("The product operation could not be completed", error));
}
```

The original exception becomes the cause of the new `ServiceOperationException`.

The error is not recovered from. Instead, the transformed exception continues downstream.

**When to use it:** When an exception should be translated into a more meaningful application-level exception while retaining the original cause.

### 2.4. `doOnError()`

`doOnError()` allows you to observe an error, usually for logging, metrics, or diagnostics.

```java
public Mono<String> demonstrateDoOnError() {
    return Mono.<String>error(new IllegalStateException("Unexpected reactive failure"))
        .doOnError(error -> log.warn("Reactive operation failed: {}", error.getMessage()));
}
```

The error is logged, but it continues downstream.

**Important:** `doOnError()` does not recover from an error or replace it. The sequence still terminates with an error unless another operator handles it later.

---

## 3. Custom Application Exceptions

The example introduces custom exceptions to distinguish between different failure scenarios.

| Exception                   | Purpose                                              |
|-----------------------------|------------------------------------------------------|
| `InvalidProductIdException` | Indicates that a product ID is invalid.              |
| `ProductNotFoundException`  | Indicates that the requested product does not exist. |
| `ServiceOperationException` | Represents a higher-level service operation failure. |

For example, the service validates a product ID and returns an error publisher when the ID is invalid:

```java
public Mono<Product> findById(Long id) {
    return Mono.defer(() -> {
        if (id == null || id <= 0) {
            return Mono.error(
                new InvalidProductIdException(id)
            );
        }

        Product product = products.get(id);

        if (product == null) {
            return Mono.error(
                new ProductNotFoundException(id)
            );
        }

        return Mono.just(product);
    });
}
```

`Mono.defer()` ensures that the validation and lookup logic executes when the publisher is subscribed to.

Returning `Mono.error()` allows failures to participate in the reactive pipeline instead of requiring the service to block while waiting for a result.

---

## 4. Centralized Error Handling in Spring WebFlux

Reactor operators handle errors within reactive pipelines. At the HTTP boundary, Spring WebFlux also needs to translate application failures into appropriate HTTP responses.

The example uses `@RestControllerAdvice` to centralize this responsibility.

```java
@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleProductNotFound(ProductNotFoundException exception) {

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("PRODUCT_NOT_FOUND", exception.getMessage())));
    }
}
```

The same approach is used for invalid IDs, service operation failures, and unexpected exceptions.

### HTTP Error Mapping

| Failure                      |                 HTTP status | Error code                 |
|------------------------------|----------------------------:|----------------------------|
| Invalid product ID           |           `400 Bad Request` | `INVALID_PRODUCT_ID`       |
| Product not found            |             `404 Not Found` | `PRODUCT_NOT_FOUND`        |
| Service operation failure    | `500 Internal Server Error` | `SERVICE_OPERATION_FAILED` |
| Unexpected application error | `500 Internal Server Error` | `INTERNAL_SERVER_ERROR`    |

The API returns a consistent error representation:

```java
public record ApiError(String code, String message) {}
```

For example, requesting a product that does not exist produces a response similar to:

```json
{
  "code": "PRODUCT_NOT_FOUND",
  "message": "Product not found with ID: 99"
}
```

The exact response depends on the requested resource and the exception raised.

**Production consideration:** Unexpected errors should generally return a generic message rather than expose internal exception details. Error codes can help clients distinguish failure types without depending on message text.

---

## 5. Exploring the API

The controller exposes endpoints for retrieving products and demonstrating the different error-handling operators.

| Method | Endpoint               | Description                       |
|--------|------------------------|-----------------------------------|
| `GET`  | `/api/products`        | Returns all products.             |
| `GET`  | `/api/products/1`      | Returns a product by ID.          |
| `GET`  | `/api/products/99`     | Demonstrates a not-found error.   |
| `GET`  | `/api/products/0`      | Demonstrates invalid-ID handling. |
| `GET`  | `/api/errors/fallback` | Demonstrates `onErrorReturn()`.   |
| `GET`  | `/api/errors/resume`   | Demonstrates `onErrorResume()`.   |
| `GET`  | `/api/errors/mapped`   | Demonstrates `onErrorMap()`.      |
| `GET`  | `/api/errors/observed` | Demonstrates `doOnError()`.       |

The error demonstration endpoints intentionally simulate failures to make the operators easier to understand.

### Example Requests

Retrieve all products:

```bash
curl http://localhost:8080/api/products
```

Retrieve an existing product:

```bash
curl http://localhost:8080/api/products/1
```

Request a product that does not exist:

```bash
curl -i http://localhost:8080/api/products/99
```

Try the fallback operator:

```bash
curl http://localhost:8080/api/errors/fallback
```

Try recovery with `onErrorResume()`:

```bash
curl http://localhost:8080/api/errors/resume
```

Observe how an exception is mapped to an HTTP 500 response:

```bash
curl -i http://localhost:8080/api/errors/mapped
```

---

## 6. How Error Handling Fits Together

The example illustrates two related responsibilities.

```text
Reactive Service
      |
      v
Mono / Flux Pipeline
      |
      +---- onErrorReturn()
      |       |
      |       +----> Fallback value
      |
      +---- onErrorResume()
      |       |
      |       +----> Alternative publisher
      |
      +---- onErrorMap()
      |       |
      |       +----> Transformed error
      |
      +---- doOnError()
      |       |
      |       +----> Observe / log error
      |
      v
Controller
      |
      v
Spring WebFlux Error Handling
      |
      v
GlobalErrorHandler
      |
      v
HTTP Error Response
```

This diagram is conceptual rather than a single execution path. Each Reactor operator is applied where appropriate, and errors that remain unhandled can reach the WebFlux exception-handling layer.

A key distinction is that errors recovered from within a pipeline do not normally reach the global exception handler as failures. For example, `onErrorReturn()` converts the error into a normal value.

---

## 7. Running the Tests

From the repository root:

```bash
mvn -pl 03-reactive-spring/reactive-error-handling clean verify
```

To run the application:

```bash
mvn -pl 03-reactive-spring/reactive-error-handling spring-boot:run
```

---

## 8. Key Takeaways

- Reactive errors are signals that travel through a `Mono` or `Flux` pipeline.
- `onErrorReturn()` provides a predefined fallback value.
- `onErrorResume()` switches to an alternative reactive publisher.
- `onErrorMap()` transforms an exception without recovering from the failure.
- `doOnError()` observes errors but does not handle or suppress them.
- Custom exceptions make application failures easier to distinguish.
- `@RestControllerAdvice` centralizes HTTP error responses in Spring WebFlux.
- `StepVerifier` tests reactive signals, while `WebTestClient` tests HTTP behavior.
- Error handling should preserve reactive composition and avoid blocking operations.

---

## Conclusion

Reactive error handling is an important part of building reliable applications with Project Reactor and Spring WebFlux.

Understanding the difference between recovering from an error, transforming it, and simply observing it makes reactive pipelines easier to reason about. Combining Reactor operators with centralized WebFlux exception handling also creates a clear separation between service-level failure behavior and HTTP response handling.

This example demonstrates these concepts through small, focused implementations and tests, providing a foundation for handling failures in more complex reactive applications.

---

## Next: 

**Reactive Testing**

The next example will explore how to test reactive applications using **Project Reactor, Reactor Test, and Spring WebFlux testing support**.

It will focus on how to verify asynchronous data streams, test reactive pipelines without blocking, and validate HTTP interactions in reactive applications.

>One focused example at a time. 🚀
