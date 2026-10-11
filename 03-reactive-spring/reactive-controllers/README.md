# Reactive Controllers

This example demonstrates how to design reactive REST controllers with Spring WebFlux.

It focuses on returning `Mono` and `Flux` from controller methods, composing reactive results, handling empty publishers, and mapping reactive results to appropriate HTTP responses.

The goal is to understand how reactive types influence controller design and how Spring WebFlux integrates publisher signals with HTTP response handling.

## What This Example Demonstrates

- Designing reactive REST controllers with Spring WebFlux
- Returning `Flux<T>` for collection endpoints
- Returning `Mono<T>` for single-resource endpoints
- Using `ResponseEntity<T>` to control HTTP responses
- Transforming publisher values with `map()`
- Handling empty publishers with `defaultIfEmpty()`
- Filtering reactive sequences using `filter()`
- Binding optional query parameters
- Testing reactive controllers with `WebTestClient`
- Distinguishing an empty collection from a missing resource

---

## What Are Reactive Controllers?

Reactive controllers are Spring WebFlux controllers that work with reactive return types such as `Mono` and `Flux`.

Instead of requiring a controller method to return a value directly, a reactive controller can return a publisher that represents the result.

For example, a controller can return a sequence of products:

```java
@GetMapping
public Flux<Product> findAll() {
    return productService.findAll();
}
```

Or it can return a single product through a `Mono`:

```java
@GetMapping("/{id}")
public Mono<Product> findById(@PathVariable Long id) {
    return productService.findById(id);
}
```

The WebFlux infrastructure integrates these publishers with HTTP request processing and response writing.

Returning a reactive type does not automatically make every operation non-blocking. The operations performed by the publisher must also avoid blocking to preserve the benefits of reactive processing.

---

## The Application

This example uses an in-memory collection of products.

Each product has an identifier, name, category, and price.

The application consists of three main components:

- `Product` — represents a product.
- `ProductService` — exposes products through `Flux` and `Mono`.
- `ProductController` — maps HTTP requests to reactive service operations.

The controller delegates to the service and returns publishers rather than manually subscribing to them.

---

## Returning a Flux

`Flux<T>` represents a publisher that can emit zero, one, or many values.

The service exposes all products as a `Flux`:

```java
public Flux<Product> findAll() {
    return Flux.fromIterable(products);
}
```

The controller returns that publisher directly:

```java
@GetMapping
public Flux<Product> findAll(@RequestParam(required = false) String category) {

    if (category == null || category.isBlank()) {
        return productService.findAll();
    }

    return productService.findByCategory(category);
}
```

Example request:

```http
GET /api/products
```

The response contains all products as a JSON array.

A category can also be supplied as a query parameter:

```http
GET /api/products?category=Electronics
```

The service filters the publisher:

```java
public Flux<Product> findByCategory(String category) {
    return Flux.fromIterable(products).filter(product -> product.category().equalsIgnoreCase(category));
}
```

The `filter()` operator allows matching products to pass downstream while excluding products that do not match the requested category.

If no products match, the `Flux` completes without emitting values. The collection endpoint returns an empty JSON array with `200 OK`.

---

## Returning a Mono

`Mono<T>` represents a publisher that emits at most one value, completes empty, or terminates with an error.

The service searches for a product by its identifier:

```java
public Mono<Product> findById(Long id) {
    return Mono.justOrEmpty(products.stream().filter(product -> product.id().equals(id)).findFirst());
}
```

If the product exists, the publisher emits it. If no product matches, the publisher completes empty.

The controller exposes this operation:

```java
@GetMapping("/{id}")
public Mono<ResponseEntity<Product>> findById(@PathVariable Long id) {

    return productService.findById(id).map(product -> ResponseEntity.ok(product))
            .defaultIfEmpty(ResponseEntity.notFound().build());
}
```

This method demonstrates how a reactive publisher can determine the HTTP response.

---

## Transforming Results with map()

The `map()` operator transforms each value emitted by a publisher.

In the controller:

```java
.map(product -> ResponseEntity.ok(product))
```

A `Product` emitted by the service becomes an HTTP 200 response containing that product.

Conceptually:

```text
Mono<Product>
      |
      v
   map(...)
      |
      v
Mono<ResponseEntity<Product>>
```

The publisher type remains a `Mono`, but the type of its emitted value changes.

---

## Handling Empty Publishers with defaultIfEmpty()

A service may return an empty `Mono` when a requested resource does not exist.

The controller handles that case with:

```java
.defaultIfEmpty(ResponseEntity.notFound().build())
```

The resulting behavior is:

```text
Product Found
     |
     v
Mono emits Product
     |
     v
map(product -> HTTP 200)
     |
     v
HTTP 200 OK
```

When no product is found:

```text
Product Not Found
     |
     v
Mono completes empty
     |
     v
defaultIfEmpty(...)
     |
     v
HTTP 404 Not Found
```

This approach keeps the response handling inside the reactive pipeline without manually subscribing to the publisher.

---

## Empty Collections Versus Missing Resources

An important distinction in REST API design is the difference between an empty collection and a missing individual resource.

### Empty collection

Request:

```http
GET /api/products?category=Furniture
```

Response:

```http
HTTP/1.1 200 OK
Content-Type: application/json

[]
```

The collection endpoint exists, but no products match the requested category.

### Missing resource

Request:

```http
GET /api/products/999
```

Response:

```http
HTTP/1.1 404 Not Found
```

The requested individual product does not exist.

The controller intentionally treats these cases differently.

An empty `Flux` naturally represents a collection with no matching elements. An empty `Mono` from a single-resource lookup can be transformed into a `404 Not Found` response.

---

## Reactive Controller Flow

A simplified view of the request lifecycle is:

```text
HTTP Request
     |
     v
ProductController
     |
     v
ProductService
     |
     v
Mono<Product> or Flux<Product>
     |
     v
Reactive Operators
     |
     +-----------------------+
     |                       |
     v                       v
Product Emitted        Publisher Empty
     |                       |
     v                       v
map(ResponseEntity)    defaultIfEmpty(...)
     |                       |
     v                       v
HTTP 200 OK            HTTP 404 Not Found
```

For collection endpoints, an empty `Flux` is serialized as an empty collection rather than being converted into a not-found response.

---

## Why Controllers Should Not Subscribe Manually

A reactive controller should return its publisher to WebFlux:

```java
@GetMapping("/{id}")
public Mono<ResponseEntity<Product>> findById(@PathVariable Long id) {

    return productService.findById(id).map(product -> ResponseEntity.ok(product))
            .defaultIfEmpty(ResponseEntity.notFound().build());
}
```

Avoid manually subscribing inside the controller:

```java
// Avoid this pattern.
@GetMapping("/{id}")
public String findById(@PathVariable Long id) {
    productService.findById(id).subscribe();
    
    return "Request received";
}
```

Manual subscription creates a separate reactive execution whose result is not naturally tied to the HTTP response. It can complicate error handling, cancellation, and response timing.

Returning the publisher allows WebFlux to coordinate subscription and response writing as part of request processing.

---

## Testing with WebTestClient

`WebTestClient` provides an API for testing WebFlux endpoints using HTTP-style requests and assertions.

The test uses `@WebFluxTest` to load the controller and the WebFlux test infrastructure:

```java
@WebFluxTest(ProductController.class)
@Import(ProductService.class)
class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;
}
```

---

## Key Concepts

### `Mono<T>`

Represents a publisher that emits at most one value, completes empty, or signals an error.

### `Flux<T>`

Represents a publisher that can emit zero, one, or many values.

### `ResponseEntity<T>`

Provides control over an HTTP response, including its status, headers, and body.

### `map()`

Transforms each emitted value into another value without changing the publisher's overall cardinality.

### `filter()`

Allows only values satisfying a predicate to pass downstream.

### `defaultIfEmpty()`

Emits a fallback value if a publisher completes without emitting a value.

### `WebTestClient`

Allows HTTP-style testing of WebFlux endpoints without requiring a running external server.

---

## What This Example Does Not Cover

This example intentionally does not cover:

- Reactive database access with R2DBC
- External HTTP calls using `WebClient`
- Functional endpoints using `RouterFunction`
- Reactive validation
- Custom exception handling
- Reactive security
- Server-Sent Events
- Advanced backpressure strategies
- Production deployment and performance tuning

These topics can be explored in separate examples.

---

## Running the Example

From the repository root, run:

```bash
mvn -pl 03-reactive-spring/reactive-controllers spring-boot:run
```

Try the endpoints:

```bash
curl http://localhost:8080/api/products
```

```bash
curl "http://localhost:8080/api/products?category=Electronics"
```

```bash
curl http://localhost:8080/api/products/1
```

```bash
curl http://localhost:8080/api/products/999
```

---

## Running Tests

Run the module's tests:

```bash
mvn -pl 03-reactive-spring/reactive-controllers test
```

Or run the full verification lifecycle:

```bash
mvn -pl 03-reactive-spring/reactive-controllers clean verify
```

---

## Key Takeaways

- Reactive controllers return publishers rather than manually subscribing to them.
- `Flux` is appropriate for collection endpoints.
- `Mono` is appropriate for single-resource lookups.
- `map()` transforms emitted values into HTTP response objects.
- `defaultIfEmpty()` can turn an empty single-resource lookup into a `404 Not Found` response.
- An empty collection can validly return `200 OK` with an empty JSON array.
- `WebTestClient` supports focused tests of reactive controller behavior.
- Reactive types alone do not guarantee that every operation is non-blocking.

---

## Next Example

**Reactive clients**

The next example will build on reactive controller fundamentals by exploring additional WebFlux capabilities and how they fit into a Spring application.