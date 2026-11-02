
# End-to-End Application Verification

This example demonstrates **end-to-end application verification** using Spring Boot and Java's HTTP Client.

It focuses on how to verify a running Spring application through its externally exposed HTTP endpoints, how to test complete business workflows across multiple Spring technologies, and how to automate end-to-end verification using GitHub Actions.

The example uses the Order Management application from the `integration-between-spring-technologies` module as the system under test.

Unlike tests that directly interact with application services, repositories, or controllers, these tests communicate with the running application through HTTP requests. This allows the verification module to test the application from an external client's perspective.

## What This Example Demonstrates

- What end-to-end application verification means.
- Testing a running Spring Boot application through HTTP.
- Using Java's `HttpClient` to send HTTP requests.
- Using Basic Authentication to authenticate HTTP requests.
- Verifying REST API responses and HTTP status codes.
- Testing successful business workflows.
- Verifying authorization and access restrictions.
- Testing customer ownership and data isolation.
- Verifying inventory consistency after order placement.
- Verifying application event processing and audit persistence.
- Testing failure scenarios and business constraints.
- Separating the verification module from the application under test.
- Automating end-to-end verification using GitHub Actions.
- Application startup and readiness verification in CI.

---

## What Is End-to-End Application Verification?

End-to-end application verification tests an application through its externally exposed interfaces to determine whether its components work together correctly.

Instead of testing individual classes or components in isolation, end-to-end verification exercises complete application workflows.

For example, placing an order involves several parts of the Order Management application:

```text
HTTP Client
    │
    ▼
REST Controller
    │
    ▼
Spring Security
    │
    ▼
Order Service
    │
    ├───────────────┐
    ▼               ▼
Inventory Service  Order Repository
    │               │
    ▼               ▼
Inventory Database  Order Database
                    │
                    ▼
              OrderPlacedEvent
                    │
                    ▼
              Audit Listener
                    │
                    ▼
              Audit Repository
                    │
                    ▼
              Audit Database
```

An end-to-end test can verify that these components work together by submitting an order and checking the resulting application behavior.

The test does not need to know how the individual services, repositories, or event listeners are implemented.

It only needs to interact with the application's HTTP endpoints and verify the expected results.

---

## Application Under Test

This example uses the Order Management application from:

```text
06-capstone-application/
└── integration-between-spring-technologies/
```

The application combines several Spring technologies:

- Spring Boot for application configuration and startup.
- Spring Web for REST API endpoints.
- Spring Data JPA for persistence.
- Spring Security for authentication and authorization.
- Spring transactions for transactional business operations.
- Spring Modulith for modular application architecture.
- Spring application events for communication between application modules.

The application provides product management, inventory management, order placement, and audit recording.

The end-to-end verification module treats this application as an external system.

It does not directly access its internal services, repositories, entities, or database.

---

## End-to-End Verification Architecture

The verification module has two main responsibilities:

1. Configure the HTTP client and connection details for the application under test.
2. Execute HTTP requests and verify the application's externally observable behavior.

The application under test runs independently, while the verification module sends requests to its configured base URL.

This separation ensures that the verification tests exercise the application through the same interfaces used by external clients.

---

## Why Use a Separate Verification Module?

End-to-end verification should test the application from outside its internal implementation.

Keeping the verification module separate provides several benefits:

- It prevents tests from depending on internal application classes.
- It verifies the actual HTTP API contract.
- It exercises Spring Security through real HTTP requests.
- It verifies interactions between different application modules.
- It allows the verification tests to run against an independently started application.
- It demonstrates how external clients interact with a Spring application.

The verification module does not duplicate business logic or application persistence components.

It only needs an HTTP client, JSON processing, and test assertions.

---

## HTTP Client

The example uses Java's built-in `HttpClient`:

```java
private final HttpClient httpClient = HttpClient.newHttpClient();
```

`HttpClient` provides an API for sending HTTP requests and receiving HTTP responses.

The example uses it to communicate with the running Order Management application.

A GET request can be constructed as follows:

```java
HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/products")).GET().build();

HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
```

The response contains the HTTP status code and response body.

For example:

```java
assertEquals(200, response.statusCode());
```

This verifies that the application successfully handled the request.

Using Java's HTTP Client demonstrates that end-to-end verification does not require Spring's `MockMvc` or direct access to Spring application components.

---

## Application Configuration

The verification module uses `application.yml` to configure the application URL and authentication credentials.

```yaml
verification:
  base-url: http://localhost:8080
  customer:
    username: customer
    password: password
  customer2:
    username: customer2
    password: password
  admin:
    username: admin
    password: password
```

The `base-url` identifies the running application.

The configured users represent different security roles:

| User      | Role     | Purpose                                |
|-----------|----------|----------------------------------------|
| customer  | CUSTOMER | Verify customer operations             |
| customer2 | CUSTOMER | Verify customer ownership restrictions |
| admin     | ADMIN    | Verify administrative operations       |

These are demonstration credentials for the local example, not production credentials.

The test class reads these values using Spring's `@Value` annotation.

---

## Basic Authentication

The Order Management application uses HTTP Basic Authentication for protected endpoints.

The verification module constructs the authorization header using the configured username and password.

```java
private HttpRequest.Builder authenticatedRequest(String path, String username, String password) {

    String credentials = username + ":" + password;

    String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

    return HttpRequest.newBuilder().uri(URI.create(baseUrl + path)).header("Authorization", "Basic " + encodedCredentials);
}
```

The resulting header follows this format:

```http
Authorization: Basic <encoded-credentials>
```

The example uses this helper to send authenticated requests as customers and administrators.

This allows the tests to verify authentication and authorization through the actual Spring Security filter chain.

---

## Testing REST API Responses

The verification tests examine HTTP status codes and response bodies.

HTTP status codes communicate the outcome of an HTTP request.

The example verifies several important responses:

| HTTP Status | Meaning      | Example                                                   |
|-------------|--------------|-----------------------------------------------------------|
| 200         | OK           | Successfully retrieving products                          |
| 201         | Created      | Successfully placing an order                             |
| 400         | Bad Request  | Submitting an invalid quantity                            |
| 401         | Unauthorized | Accessing a protected endpoint without authentication     |
| 403         | Forbidden    | Accessing an administrator endpoint as a customer         |
| 404         | Not Found    | Requesting an unknown product or another customer's order |
| 409         | Conflict     | Attempting to order more stock than is available          |

For example:

```java
HttpResponse<String> response = get("/api/products");

assertEquals(200, response.statusCode());
```

The response body can be processed using Jackson:

```java
JsonNode products = objectMapper.readTree(response.body());

assertTrue(products.isArray());
assertFalse(products.isEmpty());
```

This verifies both the HTTP response and the structure of the returned JSON.

---

## Testing the Complete Order Workflow

The main test is implemented in:

```text
OrderManagementEndToEndTest.java
```

It verifies a successful order management workflow from an external client's perspective.

The workflow follows these steps:

```text
Retrieve Products
       │
       ▼
Identify Mouse Product
       │
       ▼
Verify Authentication
       │
       ▼
Verify Authorization
       │
       ▼
Retrieve Initial Inventory
       │
       ▼
Place Order
       │
       ▼
Verify Order Creation
       │
       ▼
Verify Inventory Deduction
       │
       ▼
Verify Customer Order History
       │
       ▼
Retrieve Individual Order
       │
       ▼
Verify Customer Ownership
       │
       ▼
Verify Audit Event Persistence
```

### 1. Retrieve Products

The test begins by retrieving the available products:

```java
HttpResponse<String> productsResponse = get("/api/products");

assertEquals(200, productsResponse.statusCode());

JsonNode products = objectMapper.readTree(productsResponse.body());

assertTrue(products.isArray());
assertFalse(products.isEmpty());
```

The test locates the Mouse product and extracts its generated ID.

It does not assume a fixed database identifier.

### 2. Verify Authentication

The test attempts to access the orders endpoint without authentication:

```java
HttpResponse<String> response = get("/api/orders");

assertEquals(401, response.statusCode());
```

This verifies that protected order endpoints require authentication.

### 3. Verify Authorization

The application restricts inventory operations to administrators.

The test verifies that a customer cannot access inventory:

```java
HttpResponse<String> response = get("/api/inventory/" + productId, customerUsername, customerPassword);

assertEquals(403, response.statusCode());
```

It then verifies that an administrator can access the same resource:

```java
HttpResponse<String> response = get("/api/inventory/" + productId, adminUsername, adminPassword);

assertEquals(200, response.statusCode());
```

This demonstrates the difference between authentication and authorization.

Authentication establishes who the user is.

Authorization determines whether that user has permission to perform the requested operation.

### 4. Place an Order

The test submits an order through the REST API:

```java
HttpResponse<String> orderResponse = post(
        "/api/orders",
        """
                {
                    "productId": %d,
                    "quantity": 1
                }
                """.formatted(productId),
        customerUsername, customerPassword);

assertEquals(201, orderResponse.statusCode());
```

The test extracts the generated order ID from the response:

```java
JsonNode createdOrder = objectMapper.readTree(orderResponse.body());

long orderId = createdOrder.path("id").asLong();

assertTrue(orderId > 0);
```

This verifies that the order was successfully created and that the application returned its identifier.

### 5. Verify Inventory Deduction

Before placing the order, the test records the available inventory quantity.

After the order is placed, it retrieves the inventory again.

```java
int updatedQuantity = objectMapper.readTree(updatedInventoryResponse.body()).path("availableQuantity").asInt();

assertEquals(availableQuantity - 1, updatedQuantity);
```

This verifies that placing an order also updates the inventory.

The test therefore checks the observable result of an interaction between the ordering and inventory modules.

### 6. Verify Order History

The test retrieves the customer's order history:

```java
HttpResponse<String> response = get("/api/orders", customerUsername, customerPassword);
```

It then verifies that the newly created order appears in the returned collection.

This confirms that the order is available through the application's order history endpoint.

### 7. Verify Individual Order Retrieval

The test retrieves the newly created order using its ID:

```java
HttpResponse<String> response = get("/api/orders/" + orderId, customerUsername, customerPassword);

assertEquals(200, response.statusCode());
```

It verifies that the returned order has the expected identifier.

### 8. Verify Customer Ownership

The application supports multiple customers.

The test attempts to retrieve an order using a different customer's credentials:

```java
HttpResponse<String> response = get("/api/orders/" + orderId, secondCustomerUsername, secondCustomerPassword);

assertEquals(404, response.statusCode());
```

The application returns `404 Not Found` when another customer attempts to retrieve the order.

This verifies that customers cannot access orders belonging to other customers.

---

## Verifying Application Events and Audit Persistence

The Order Management application uses Spring application events to communicate between the ordering and audit modules.

When an order is successfully placed, the ordering module publishes an `OrderPlacedEvent`.

The audit module listens for this event:

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void on(OrderPlacedEvent event) {
    AuditEntry entry = new AuditEntry("ORDER_PLACED", event.orderId(), "Order placed by " + event.customerUsername()
            + " with total amount " + event.totalAmount(), event.occurredAt());

    auditRepository.save(entry);
}
```

The listener uses `AFTER_COMMIT` to process the event after the order transaction has successfully committed.

It uses `REQUIRES_NEW` to persist the audit entry in a separate transaction.

This ensures that audit persistence is not part of the original order transaction.

### Audit REST Endpoint

The audit module exposes a read-only endpoint:

```http
GET /api/audit
```

The endpoint returns audit entries ordered by occurrence time.

It is restricted to users with the ADMIN role.

The verification test checks that:

- An unauthenticated request returns `401 Unauthorized`.
- A customer request returns `403 Forbidden`.
- An administrator request returns `200 OK`.
- The returned audit entries contain an `ORDER_PLACED` event for the newly created order.

The test verifies the order ID and event type in the same audit entry.

This demonstrates how end-to-end verification can test application event processing through an externally observable HTTP endpoint without directly accessing the audit repository or database.

---

## Testing Audit Persistence Locally

The audit endpoint delegates to `AuditRepository.findAllByOrderByOccurredAtDesc()` to retrieve audit entries ordered by occurrence time in descending order.

This implementation is exercised through the end-to-end verification tests, which place an order and subsequently retrieve the corresponding audit entry through `GET /api/audit`.

To execute this verification locally:

1. Start the Integration Between Spring Technologies application.
2. Run the End-to-End Application Verification tests.
3. Verify that the audit endpoint returns the expected `ORDER_PLACED` entry.

Start the integration application from the repository root:

```bash
mvn -pl 06-capstone-application/integration-between-spring-technologies \
org.springframework.boot:spring-boot-maven-plugin:run
```

In another terminal, execute

```bash
mvn -pl 06-capstone-application/end-to-end-application-verification \
-Dskip.e2e.tests=false test
```

The integration application must remain running while the verification tests execute.

This approach verifies the audit repository implementation through the application's externally exposed REST API without introducing a separate test that duplicates the end-to-end verification.


---

## Testing Failure Scenarios

Failure scenarios are implemented separately in:

```text
OrderManagementFailureScenarioTest.java
```

Separating these tests keeps the successful workflow focused while allowing business failure conditions to be verified independently.

### 1. Invalid Order Quantity

The application should reject an order with a quantity of zero.

```java
HttpResponse<String> response = post(
        "/api/orders",
        """
                {
                    "productId": %d,
                    "quantity": 0
                }
                """.formatted(productId),
        customerUsername, customerPassword);

assertEquals(400, response.statusCode());
```

This verifies that the application validates order quantities.

### 2. Insufficient Inventory

The application should reject an order when the requested quantity exceeds available stock.

```java
HttpResponse<String> response = post(
        "/api/orders",
        """
                {
                    "productId": %d,
                    "quantity": 100000
                }
                """.formatted(productId),
        customerUsername, customerPassword);

assertEquals(409, response.statusCode());
```

This verifies that the application enforces inventory constraints.

### 3. Unknown Product

The application should reject an order referencing a product that does not exist.

```java
long unknownProductId = Long.MAX_VALUE;
```

The test submits an order using this identifier and verifies that the application responds with:

```java
assertEquals(404, response.statusCode());
```

This verifies that the application handles requests referencing unknown resources correctly.

---

## Why Use Separate Test Classes?

The example separates successful workflows from failure scenarios.

### OrderManagementEndToEndTest

This class focuses on:

- Successful product retrieval.
- Authentication and authorization.
- Order creation.
- Inventory deduction.
- Order history.
- Individual order retrieval.
- Customer ownership.
- Audit event persistence.

### OrderManagementFailureScenarioTest

This class focuses on:

- Invalid order quantities.
- Insufficient inventory.
- Unknown products.

This organization makes the tests easier to understand and maintain while keeping related verification scenarios together.

---

## Running the Example

The verification module communicates with the independently running Order Management application.

Therefore, the application under test must be started before executing the verification tests locally.

### 1. Start the Order Management Application

From the repository root, run:

```bash
mvn -pl 06-capstone-application/integration-between-spring-technologies \
org.springframework.boot:spring-boot-maven-plugin:run
```

The application starts on:

```text
http://localhost:8080
```

The product endpoint can be used to confirm that the application is responding:

```text
http://localhost:8080/api/products
```

Keep this application running while executing the verification tests.

### 2. Run the End-to-End Verification Tests

Open another terminal and execute:

```bash
mvn -pl 06-capstone-application/end-to-end-application-verification \
-Dskip.e2e.tests=false test
```

The verification module sends HTTP requests to the running application and checks the expected responses.

**Important:** Running the verification tests without starting the Order Management application will result in a connection error because the verification module does not start the target application automatically.

---

## Continuous Integration with GitHub Actions

The example includes a GitHub Actions workflow:

```text
.github/workflows/end-to-end-verification.yml
```

The workflow automates the end-to-end verification process whenever changes are pushed to the `main` branch or a pull request is opened.

It can also be triggered manually using `workflow_dispatch`.

### CI Workflow

The workflow performs the following operations:

```text
GitHub Actions
      │
      ▼
Check Out Repository
      │
      ▼
Set Up Java 21
      │
      ▼
Build Order Management Application
      │
      ▼
Start Order Management Application
      │
      ▼
Wait for Application Readiness
      │
      ▼
Run End-to-End Verification Tests
      │
      ▼
Stop Order Management Application
      │
      ▼
Upload Logs if Verification Fails
```

### 1. Build the Application

The workflow builds the Order Management application and its required Maven dependencies:

```bash
mvn -B -pl 06-capstone-application/integration-between-spring-technologies \
    -am package -DskipTests
```

### 2. Start the Application

The workflow starts the application in the background using the Spring Boot Maven plugin.

Application output is redirected to a log file so that it can be inspected if startup or verification fails.

### 3. Verify Application Readiness

Before running the tests, the workflow repeatedly checks:

```text
http://localhost:8080/api/products
```

It also verifies that the response contains a non-empty JSON array.

This prevents the verification tests from starting before the application is ready.

The workflow also detects if the application process terminates unexpectedly.

### 4. Run Verification Tests

Once the application is ready, the workflow executes:

```bash
mvn -B \
  -pl 06-capstone-application/end-to-end-application-verification \
  -Dskip.e2e.tests=false \
  test
```

### 5. Stop the Application

A shell cleanup trap ensures that the background application process is stopped when the workflow step exits.

### 6. Upload Application Logs

If the workflow fails, the application logs are uploaded as a GitHub Actions artifact.

This makes it easier to investigate application startup failures and end-to-end verification failures.

---

## End-to-End Verification vs Integration Testing

End-to-end verification and integration testing are related, but they focus on different aspects of application behavior.

### Integration Testing

Integration testing verifies that application components work together correctly.

For example:

- A service interacts with a repository.
- A controller invokes a service.
- A transaction correctly manages database operations.
- An application event listener processes a published event.

Integration tests commonly use Spring's testing support and may interact directly with application components.

### End-to-End Verification

End-to-end verification exercises the running application through its externally exposed interfaces.

For example:

- An HTTP client places an order.
- The application authenticates the customer.
- The ordering module processes the request.
- Inventory is updated.
- An application event is published.
- An audit entry is persisted.
- The HTTP client verifies the resulting responses.

The verification module does not need to know how these internal operations are implemented.

The distinction can be summarized as:

| Integration Testing                         | End-to-End Verification                   |
|---------------------------------------------|-------------------------------------------|
| Verifies interactions between components    | Verifies complete application workflows   |
| May use internal application components     | Uses externally exposed interfaces        |
| Often uses Spring test support              | Uses an independent HTTP client           |
| Can run within the application test context | Requires a running application            |
| Focuses on component integration            | Focuses on externally observable behavior |

Both approaches are useful and complement each other.

---

## Important Testing Considerations

### Independent Application Startup

The verification module does not start the Order Management application automatically.

The target application must be running before the tests execute locally.

The GitHub Actions workflow handles this automatically.

### Test Data

The Order Management application uses an in-memory H2 database.

Products are initialized when the application starts if no products exist.

The verification tests locate the Mouse product by name rather than assuming a fixed generated ID.

Restarting the application resets the in-memory database and its data.

### External Dependencies

The verification module communicates with the application over HTTP.

It does not require direct access to the application's database or internal Spring beans.

This keeps the verification tests independent of the target application's internal implementation.

### Test Environment

The example uses local demonstration credentials and an in-memory database.

A production end-to-end verification environment would normally use dedicated test credentials, controlled test data, and an appropriately isolated application environment.

---

## Key Takeaways

- End-to-end verification tests an application through its externally exposed interfaces.
- Java's `HttpClient` can be used to send real HTTP requests to a running Spring Boot application.
- Jackson can be used to inspect JSON responses.
- HTTP status codes help verify application behavior and error handling.
- Basic Authentication allows verification of protected REST endpoints.
- End-to-end tests can verify complete workflows involving multiple Spring technologies.
- Inventory consistency can be verified through externally observable API responses.
- Application event processing and audit persistence can be verified through a read-only endpoint.
- Separate test classes help organize successful workflows and failure scenarios.
- A separate verification module avoids coupling tests to internal application components.
- GitHub Actions can automate application startup, readiness checks, and end-to-end verification.
- End-to-end verification complements unit and integration testing rather than replacing them.

---

## Next Example

The next example begins **End-to-End Application Verification** 
