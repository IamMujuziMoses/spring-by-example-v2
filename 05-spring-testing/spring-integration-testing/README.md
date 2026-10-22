# Integration Testing

This example demonstrates **integration testing** in Spring Boot.

It focuses on how to verify that multiple application components work together correctly, including the web layer, service layer, repository layer, and database.

The example uses `@SpringBootTest`, `MockMvc`, Spring Data JPA, and an H2 in-memory database to test application behavior across these components.

## What This Example Demonstrates

- What integration testing means in a Spring application.
- Loading the application context with `@SpringBootTest`.
- Testing HTTP endpoints with `MockMvc`.
- Integrating controllers, services, and repositories.
- Persisting and retrieving entities with Spring Data JPA.
- Using an H2 in-memory database for repeatable tests.
- Verifying HTTP responses and database state.
- Testing successful operations and not-found responses.
- The difference between integration tests, unit tests, and MVC slice tests.

---

## What Is Integration Testing?

Integration testing verifies that multiple components work together correctly.

In a Spring application, individual components may work correctly in isolation but still encounter problems when integrated. For example, a controller might call a service that interacts with a repository, but the application could have incorrect configuration, dependency injection, persistence mappings, or transaction behavior.

Integration tests help identify these problems by exercising interactions between components.

A simplified application flow looks like:

```
HTTP Request
     │
     ▼
Controller
     │
     ▼
Service
     │
     ▼
Repository
     │
     ▼
Database
     │
     ▼
HTTP Response
```

This example follows that flow by creating and retrieving messages through HTTP endpoints and verifying their persistence.

---

## Application Structure

The example implements a small message API.

The application consists of the following components:

- **IntegrationTestingApplication** — bootstraps the Spring Boot application.
- **MessageController** — handles HTTP requests for creating and retrieving messages.
- **MessageService** — contains the message operations and coordinates persistence.
- **MessageRepository** — provides database access through Spring Data JPA.
- **Message** — represents a message stored in the database.
- **MessageNotFoundException** — represents a request for a message that does not exist.
- **MessageIntegrationTest** — verifies interactions across the application components.

The controller does not access the repository directly. Instead, it delegates to the service, which uses the repository to perform persistence operations.

This keeps the example small while demonstrating a typical layered Spring application.

---

## The Role of `@SpringBootTest`

The example uses:

```java
@SpringBootTest
@AutoConfigureMockMvc
class MessageIntegrationTest {
    // Integration tests
}
```

`@SpringBootTest` loads the Spring Boot application context using the application's configuration.

This allows Spring to configure the components required by the application, including:

- The message controller.
- The message service.
- The Spring Data JPA repository.
- The database connection.
- The persistence infrastructure.

Rather than manually constructing the components, the test uses the same Spring-managed components that the application uses.

This helps verify that dependency injection and application configuration work together correctly.

---

## Testing HTTP Requests with `MockMvc`

The example uses `MockMvc` to exercise the application's HTTP endpoints.

```java
@Autowired
private MockMvc mockMvc;
```

`MockMvc` performs requests through Spring MVC without requiring a real HTTP server to be started.

For example, a request to create a message can be performed as follows:

```java
mockMvc.perform(post("/messages")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
                {
                    "content": "Integration testing"
                }
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("Integration testing"));
```

The test sends a JSON request body and verifies that the application responds with HTTP `201 Created` and the expected message content.

The request passes through the MVC infrastructure, controller, service, repository, and database.

This makes `MockMvc` useful for integration tests that need to exercise the web layer without starting an external server.

---

## Creating and Persisting a Message

The application exposes a `POST /messages` endpoint.

The controller delegates message creation to the service:

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public Message createMessage(@RequestBody CreateMessageRequest request) {
    return messageService.createMessage(request.content());
}
```

The service creates a new entity and saves it through the repository:

```java
@Transactional
public Message createMessage(String content) {
    return messageRepository.save(new Message(content));
}
```

Spring Data JPA handles the persistence operation.

The integration test verifies both the HTTP response and the resulting database state:

```java
mockMvc.perform(post("/messages")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
                {
                    "content": "Integration testing"
                }
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.content").value("Integration testing"));

assertThat(messageRepository.findAll())
        .extracting(Message::getContent)
        .containsExactly("Integration testing");
```

The first part verifies the response returned by the application.

The second part queries the repository and confirms that the message was persisted.

Together, these assertions verify more than the controller's response: they also confirm that the request resulted in the expected database change.

---

## Retrieving a Persisted Message

The application exposes a `GET /messages/{id}` endpoint.

The service retrieves the entity through the repository:

```java
@Transactional(readOnly = true)
public Message getMessage(Long id) {
    return messageRepository.findById(id).orElseThrow(() -> new MessageNotFoundException(id));
}
```

The integration test first persists a message, then retrieves it through an HTTP request:

```java
Message savedMessage = messageRepository.save(new Message("Persisted message"));

mockMvc.perform(get("/messages/{id}", savedMessage.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(savedMessage.getId()))
        .andExpect(jsonPath("$.content").value("Persisted message"));
```

This verifies that the controller can retrieve a previously persisted entity through the service and repository layers.

The test uses the repository to arrange the initial database state and `MockMvc` to exercise the HTTP endpoint.

---

## Testing Error Responses

Integration tests should also verify how the application behaves when an operation cannot be completed.

When a requested message does not exist, the service throws `MessageNotFoundException`.

The exception is annotated with `@ResponseStatus(HttpStatus.NOT_FOUND)`, so Spring MVC translates it into an HTTP `404 Not Found` response.

The test verifies this behavior:

```java
mockMvc.perform(get("/messages/{id}", 999999L)).andExpect(status().isNotFound());
```

This checks that the exception raised by the service is translated into the expected HTTP response by the web layer.

It therefore tests an interaction between application logic and Spring MVC's exception handling.

---

## Using an H2 In-Memory Database

The example uses H2 as its database.

The following configuration is provided in `application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:integrationtestdb;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
```

The database runs in memory, so the example does not require an external database installation.

Hibernate creates the schema for the JPA entity when the persistence context starts and drops the schema when the relevant context is closed.

This makes the example convenient for local development and automated testing.

An in-memory database is particularly useful when tests need a predictable database environment. However, it does not reproduce every behavior of a production database. Applications that depend on database-specific SQL, constraints, or vendor-specific features may also need integration tests against the actual database engine.

---

## Keeping Tests Independent

The example uses a `@BeforeEach` method to clear existing messages before each test:

```java
@BeforeEach
void setUp() {
    messageRepository.deleteAll();
}
```

This helps ensure that each test starts with a predictable database state.

For example, the creation test expects to find exactly one message after its request. Without clearing existing records, that assertion could depend on data left by another test.

Test isolation is important because tests should not rely on execution order or on changes made by other tests.

For more complex applications, test data can be managed with transactions, dedicated test fixtures, or database cleanup strategies appropriate to the persistence architecture.

---

## Integration Testing vs. Other Testing Approaches

Different testing approaches serve different purposes.

### Unit Testing

Unit tests verify individual classes or methods in isolation.

For example, a unit test could instantiate `MessageService` with a mocked repository and verify how it handles a repository result.

```
Unit Test
    │
    ▼
Single Component
    │
    ▼
Assertions
```

Unit tests are generally fast and useful for testing business logic without starting Spring infrastructure.

### MVC Slice Testing

A test using `@WebMvcTest` focuses on the Spring MVC layer.

The test loads a limited application context and commonly replaces service dependencies with mocks.

```
MVC Slice Test
      │
      ▼
Spring MVC
      │
      ▼
Mocked Dependencies
```

This is useful for testing request mappings, validation, serialization, and HTTP responses without loading the entire application.

### Standalone MockMvc Testing

Standalone `MockMvc` tests construct a controller directly and configure MockMvc without loading the full Spring application context.

This is useful when controller behavior is the main concern.

### Integration Testing

This example loads the application context and uses the real Spring-managed service, repository, and database infrastructure.

```
Integration Test
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
Repository
       │
       ▼
Database
```

The purpose is to verify that the components work together, rather than testing each component independently.

---

## Why Use `MockMvc` Instead of a Real HTTP Server?

`MockMvc` exercises Spring MVC's request-processing infrastructure without starting a real web server.

This offers several benefits:

- Tests can run without binding to a network port.
- Requests and responses can be asserted directly.
- Tests can run efficiently in automated builds.
- The full application context and persistence layer can still participate in the test.

However, `MockMvc` does not verify actual network communication or all server-specific behavior.

For tests that need to verify the application through a real HTTP connection, `@SpringBootTest` can instead be configured with `webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT`, together with an HTTP client such as `TestRestTemplate` or `WebTestClient`, as appropriate for the application.

---

## Running the Example

From the repository root, run:

```bash
mvn -pl 05-spring-testing/integration-testing -am test
```

The `-pl` option selects the integration-testing module, while `-am` also builds required Maven modules in the reactor.

To run the application independently:

```bash
mvn -pl 05-spring-testing/integration-testing spring-boot:run
```

The application exposes the following endpoints:

| Method | Endpoint         | Description                    |
|--------|------------------|--------------------------------|
| `POST` | `/messages`      | Creates and persists a message |
| `GET`  | `/messages/{id}` | Retrieves a message by its ID  |

Example request body:

```json
{
  "content": "Integration testing"
}
```

The creation endpoint returns HTTP `201 Created` with the persisted message, including its generated ID.

---

## Running the Tests

To run the tests from within the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 05-spring-testing/integration-testing test
```

The integration tests verify:

- Message creation through HTTP.
- Persistence of the created message.
- Retrieval of an existing message.
- The HTTP `404 Not Found` response for a missing message.

---

## Key Takeaways

- Integration testing verifies that multiple application components work together.
- `@SpringBootTest` loads the Spring Boot application context.
- `MockMvc` exercises Spring MVC without starting a real HTTP server.
- Spring Data JPA provides repository operations for persistence.
- H2 provides a lightweight database for repeatable tests.
- Tests can verify both HTTP responses and database state.
- Integration tests help identify configuration and component-interaction problems that isolated unit tests may not detect.
- `@WebMvcTest` and standalone `MockMvc` serve different purposes from full application integration tests.
- A clean, predictable database state helps keep tests independent.
- H2 is convenient for testing, but production-database compatibility may require additional tests.

---

## What This Example Does Not Cover

This example intentionally focuses on basic integration testing across the web, service, and persistence layers.

It does not cover:

- Integration testing against a production database engine.
- Testcontainers and containerized dependencies.
- External services and HTTP client integration.
- Distributed transactions.
- Messaging and asynchronous integration.
- Full end-to-end tests through a deployed application.
- Performance and load testing.
- Advanced test data management.

These topics can be explored separately as the Spring Testing module expands.

---

## Next Example

The next example will explore **Testcontainers**, demonstrating how to run tests against containerized infrastructure and improve confidence in integration tests that depend on external services or databases.
